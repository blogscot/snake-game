(ns clojure-snake.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clojure.string :as str])
  (:gen-class
   :methods [^{:static true} [game [String int] String]])
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane WindowConstants)
           (java.awt.event ActionListener KeyListener KeyEvent))
  (:require [clojure-snake.gpset :refer [create-snake create-apple direction steps score RIGHT LEFT UP DOWN move snake apple game-over?]]))

; ---------------------------------------------------------------------
; functional model
; ---------------------------------------------------------------------
; constants to describe time, space and motion
(def width 50)
(def height 30)
(def point-size 15)
(def turn-millis 400)
(def win-length 50)
(def dirs {KeyEvent/VK_LEFT LEFT
           KeyEvent/VK_RIGHT RIGHT
           KeyEvent/VK_UP UP
           KeyEvent/VK_DOWN DOWN})

(def routine (ref {}))
(def pause? (ref false))

(defn point-to-screen-rect
  "Returns the origin x, y plus dx and dy needed
   to draw a square of size point."
  [[x y]] (map #(* point-size %) [x y 1 1]))

(defn player-win? [{body :body}] (>= (count body) win-length))

; function that changes direction
(defn turn [snake newdir]
  (assoc snake :dir newdir))

; ---------------------------------------------------------------------
; mutable model
; ---------------------------------------------------------------------
; function that resets the game state
(defn reset-game [snake apple]
  (dosync (ref-set snake (create-snake))
          (ref-set apple (create-apple))
          (ref-set direction RIGHT)
          (ref-set steps 0)
          (ref-set score 0)
          (ref-set pause? false))
  nil)

; function for updating snake's direction
(defn update-direction [snake newdir]
  (when newdir (dosync (ref-set direction newdir))))

; function for updating positions of snake and apple
(defn update-positions [snake apple]
  (dosync
   (alter snake move @direction @apple))
  nil)

; ---------------------------------------------------------------------
; util
; ---------------------------------------------------------------------
(defn setup-routine
  [string]
  (clojure.string/replace string (re-pattern "\\((?!do)") "(clojure-snake.gpset/"))

; ---------------------------------------------------------------------
; gui
; ---------------------------------------------------------------------
; function for making a point on the screen
(defn fill-point [g pt color]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g color)
    (.fillRect g x y width height)))


; function for painting snakes and apples
(defmulti paint (fn [_g object & _] (:type object)))

(defmethod paint :apple [g {:keys [location color]}]
  (fill-point g location color))

(defmethod paint :snake [g {:keys [body color]}]
  (doseq [point body]
    (fill-point g point color)))

(defn destroy-snake
  []
  {:body '([-10 -10])
   :type :snake
   :color (Color. 15 160 70)
   :score 0})

(defn update-game [frame e]
  (if (empty? @routine)
    (update-positions snake apple)
    (dosync (try
              (eval (read-string @routine))
              (catch Exception _ex
                (do
                  (JOptionPane/showMessageDialog frame
                                                 "There is an error in control routine. Check the documentation to see how to form a valid control routine."
                                                 "Control routine error" JOptionPane/ERROR_MESSAGE)
                  (.stop (.getSource e))
                  (.dispose frame)))))))

(defn prompt-play-again [frame e]
  (if (= JOptionPane/YES_OPTION
         (JOptionPane/showConfirmDialog
          frame (str "Apples eaten: " @score "\n Play again?")
          "Game over!"
          JOptionPane/YES_NO_OPTION))
    (reset-game snake apple)
    (do
            ;(.removeActionListener (.getSource e) this)
      (.stop (.getSource e))
      (.dispose frame))))

(defn game-panel [frame snake apple]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (paint g @apple))
    (actionPerformed [e]
      (when (false? @pause?)
        (update-game frame e))
      (when (game-over? @snake)
        (prompt-play-again frame e))
      (when (player-win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You win!"))
      (.repaint this))
    (keyPressed [e]
      (let [key-code (.getKeyCode e)]
        (when (empty? @routine)
          (update-direction snake (dirs key-code)))
        (when (or (= key-code KeyEvent/VK_E) (= key-code KeyEvent/VK_ESCAPE))
          (dosync
           (ref-set snake (destroy-snake))))
        (when (= key-code KeyEvent/VK_P)
          (dosync
           (ref-set pause? (not @pause?))))))
    (getPreferredSize []
      (Dimension. (* (inc width) point-size)
                  (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

; main game function
(defn game [rtn speed]
  (dosync
   (ref-set snake (create-snake))
   (ref-set apple (create-apple))
   (ref-set direction RIGHT)
   (ref-set steps 0)
   (ref-set score 0)
   (ref-set routine (when (seq rtn)
                      (setup-routine rtn)))
   (let [frame (JFrame. "Snake game - (press ESCAPE or E to exit the game, press P to toggle pause)")
         panel (game-panel frame snake apple)
         timer (Timer. (- turn-millis (* 10 speed)) panel)]
     (doto panel
       (.setFocusable true)
       (.addKeyListener panel))
     (doto frame
       (.add panel)
       (.pack)
       (.setVisible true)
       (.setResizable false)
       (.setDefaultCloseOperation WindowConstants/DO_NOTHING_ON_CLOSE))
     (.start timer)
     (str [snake, apple, timer]))))

;; (defn -game
;;   [rtn speed]
;;   (game rtn speed))

(def cli-options
  ;; An option with a required argument
  [["-r" "--routine ROUTINE" "Control routine"
    :id :routine
    :default ""]
   ["-s" "--speed SPEED" "Snake speed"
    :default 25
    :id :speed
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 40) "Must be a number between 0 and 40"]]])

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)
        {:keys [routine speed]} (:options opts)]
    (game routine speed)))
