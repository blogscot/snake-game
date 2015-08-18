(ns clojure-snake.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class
   :methods [#^{:static true} [game [String int] String]])
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane WindowConstants)
           (java.awt.event ActionListener KeyListener))
  (:use clojure-snake.util.import-static)
  (:use clojure-snake.gpset))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN VK_E VK_ESCAPE VK_P)

; ---------------------------------------------------------------------
; functional model
; ---------------------------------------------------------------------
; constants to describe time, space and motion
(def width 19)
(def height 10)
(def point-size 50)
(def turn-millis 100)
(def win-length 211)
(def dirs {VK_LEFT  [-1 0]
           VK_RIGHT [1 0]
           VK_UP    [0 -1]
           VK_DOWN  [0 1]})

(def routine (ref {}))
(def pause? (ref false))

(defn point-to-screen-rect [pt]
  (map #(* point-size %)
       [(pt 0) (pt 1) 1 1]))

; function for checking if the player won
(defn win? [{body :body}]
  (>= (count body) win-length))

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
(defmulti paint (fn [g object & _] (:type object)))

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

; game panel
(defn game-panel [frame snake apple]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (paint g @apple))
    (actionPerformed [e]

      (if (= @pause? false)
        (if (nil? @routine)
          (do
            ;(str (println "nil routine" @routine))
            (update-positions snake apple))
          (do
            ;(str (println "routine " @routine))
            (dosync (try
                      (eval (read-string @routine))
                      (catch Exception ex
                        (do
                          (JOptionPane/showMessageDialog frame
                          "There is an error in control routine. Check the documentation to see how to form a valid control routine."
                          "Control routine error" JOptionPane/ERROR_MESSAGE)
                          (.stop (.getSource e))
                          (.dispose frame))))))))

      (if (lose? @snake)
        (if (= JOptionPane/YES_OPTION (JOptionPane/showConfirmDialog frame (str "Apples eaten: " @score "\n Play again?") "Game over!" JOptionPane/YES_NO_OPTION))
          (reset-game snake apple)
          (do
            ;(.removeActionListener (.getSource e) this)
            (.stop (.getSource e))
            (.dispose frame))))
      (when (win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You win!"))
      (.repaint this))
    (keyPressed [e]
      (if (nil? @routine)
        (update-direction snake (dirs (.getKeyCode e))))
      (if (or (= (.getKeyCode e) VK_E) (= (.getKeyCode e) VK_ESCAPE))
        (dosync
         (ref-set snake (destroy-snake))))
       (if (or (= (.getKeyCode e) VK_P))
        (dosync
         (ref-set pause? (not @pause?)))))
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
   (ref-set routine (if ((complement nil?) rtn)
                      (setup-routine rtn)))
   (let [frame (JFrame. "Snake game - (press ESCAPE or E to exit the game, press P to toogle pause)")
        panel (game-panel frame snake apple)
        timer (Timer. (- turn-millis speed) panel)]
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

(defn -game
  [rtn speed]
  (game rtn speed))

(def cli-options
  ;; An option with a required argument
  [["-r" "--routine ROUTINE" "Control routine"
    :id :routine
    :default ""]
   ["-s" "--speed SPEED" "Snake speed"
    :default 25
    :id :speed
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 50) "Must be a number between 0 and 50"]]])

(defn -main
  [& args]
  (let [opts (parse-opts args cli-options)]
    (game (if (= "" (:routine (:options opts))) nil (str (:routine (:options opts)))) (:speed (:options opts)))))
