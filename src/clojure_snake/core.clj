(ns clojure-snake.core
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane WindowConstants)
           (java.awt.event ActionListener KeyListener))
  (:use clojure-snake.util.import-static))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)

; ---------------------------------------------------------------------
; functional model
; ---------------------------------------------------------------------
; constants to describe time, space and motion
(def width 19)
(def height 10)
(def point-size 25)
(def turn-millis 100)
(def win-length 220)
(def dirs {VK_LEFT  [-1 0]
           VK_RIGHT [1 0]
           VK_UP    [0 -1]
           VK_DOWN  [0 1]})

; math functions for game board
(defn add-points [& pts]
  (vec (apply map + pts)))

(defn point-to-screen-rect [pt]
  (map #(* point-size %)
       [(pt 0) (pt 1) 1 1]))

; function for creating an apple
(defn create-apple []
  {:location [(rand-int width) (rand-int height)]
   :color (Color. 210 50 90)
   :type :apple})

; function for creating a snake
(defn create-snake []
  {:body (for [x (range 8 -1 -1)] [x 10])
   :dir [1 0]
   :type :snake
   :color (Color. 15 160 70)})

; function for moving a snake
(defn move [{:keys [body dir] :as snake} & grow]
  (assoc snake :body (cons (add-points (first body) dir)
                           (if grow body (butlast body)))))

; function for checking if the player won
(defn win? [{body :body}]
  (>= (count body) win-length))

; function for checking if the player lost the game,
; which means that head of the snake has overlaped
; with its body
(defn head-overlaps-body? [{[head & body] :body}]
  (contains? (set body) head))

(defn out-of-bounds? [{[head] :body}]
  (or (< (head 0) 0)
      (> (head 0) width)
      (< (head 1) 0)
      (> (head 1) height)))

(defn lose? [snake]
  (or (head-overlaps-body? snake) (out-of-bounds? snake)))

; function for checking if the snake eats an apple
; (check if head location equals apple location)
(defn eats? [{[snake-head] :body} {apple :location}]
  (= snake-head apple))

; function that changes direction
(defn turn [snake newdir]
  (assoc snake :dir newdir))

; ---------------------------------------------------------------------
; mutable model
; ---------------------------------------------------------------------
; function that resets the game state
(defn reset-game [snake apple]
  (dosync (ref-set snake (create-snake))
          (ref-set apple (create-apple)))
  nil)

; function for updating snake's direction
(defn update-direction [snake newdir]
  (when newdir (dosync (alter snake turn newdir))))

; function for updating positions of snake and apple
(defn update-positions [snake apple]
  (dosync
    (if (eats? @snake @apple)
      (do (ref-set apple (create-apple))
          (alter snake move :grow))
      (alter snake move)))
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
      (when (lose? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "Game over!"))
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
  (let [snake (ref (create-snake))
        apple (ref (create-apple))
        frame (JFrame. "Snake")
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
    [snake, apple, timer]))
