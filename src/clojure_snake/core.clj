(ns clojure-snake.core
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
(def point-size 25)
(def turn-millis 150)
(def win-length 211)
(def dirs {VK_LEFT  [-1 0]
           VK_RIGHT [1 0]
           VK_UP    [0 -1]
           VK_DOWN  [0 1]})

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
  (comment (when newdir (dosync (ref-set direction newdir)))))

; function for updating positions of snake and apple
(defn update-positions [snake apple]
  (dosync
    (if (eats? @snake @apple)
      (do
          (alter snake move @direction @apple :grow)
          )
      (alter snake move @direction @apple)))
  nil)

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
      (update-positions snake apple)
      (dosync
       (eval routine))
      (when (lose? @snake)
        (JOptionPane/showMessageDialog frame (str "Game over! Apples eaten: " @score))
        (reset-game snake apple))
      (when (win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You win!"))
      (.repaint this))
    (keyPressed [e]
      (update-direction snake (dirs (.getKeyCode e))))
    (getPreferredSize []
      (Dimension. (* (inc width) point-size)
                  (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

; main game function
(defn game []
  (dosync
   (ref-set snake (create-snake))
   (ref-set apple (create-apple))
   (ref-set direction RIGHT)
   (ref-set steps 0)
   (ref-set score 0)
   (let [frame (JFrame. "Snake")
        panel (game-panel frame snake apple)
        timer (Timer. turn-millis panel)]
    (doto panel
      (.setFocusable true)
      (.addKeyListener panel))
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true)
      (.setResizable false)
      (.setDefaultCloseOperation WindowConstants/EXIT_ON_CLOSE))
    (.start timer)
    [snake, apple, timer])))
