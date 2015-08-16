(ns clojure-snake.core
  (:gen-class
   :methods [#^{:static true} [game [String int] String]])
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane WindowConstants)
           (java.awt.event ActionListener KeyListener))
  (:use clojure-snake.util.import-static)
  (:use clojure-snake.gpset))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)

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
          (ref-set score 0))
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
(defn insert-into-string
  [string new index]
  (if ((complement neg?) index)
    (str (subs string 0 index) new (subs string index))
    string))

(defn find-index-of-char
  [string charc start]
  (let [index (.indexOf string charc start)]
    (if (> index -1)
      (inc index)
      index)))

(defn setup-routine
  [string new-string crit]
  (loop [res string index 0]
    (if (< index 0)
      res
      (recur (insert-into-string res new-string (find-index-of-char res crit index))
             (find-index-of-char res crit (inc index))))))

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

; game panel
(defn game-panel [frame snake apple]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (paint g @apple))
    (actionPerformed [e]

      (if (nil? @routine)
        (do
          ;(str (println "nil routine" @routine))
          (update-positions snake apple))
        (do
          ;(str (println "routine " @routine))
          (dosync (eval (read-string @routine)))))

      (when (lose? @snake)
        (JOptionPane/showMessageDialog frame (str "Game over! Apples eaten: " @score))
        (reset-game snake apple))
      (when (win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You win!"))
      (.repaint this))
    (keyPressed [e]
      (if (nil? @routine)
        (update-direction snake (dirs (.getKeyCode e)))))
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
                      (setup-routine rtn "clojure-snake.gpset/" "(")))
   (let [frame (JFrame. "Snake")
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
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE))
    (.start timer)
    (str [snake, apple, timer]))))

(defn -game
  [rtn speed]
  (game rtn speed))
