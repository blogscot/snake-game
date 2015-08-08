(ns clojure-snake.gpset
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane WindowConstants)
           (java.awt.event ActionListener KeyListener))
  (:use clojure-snake.util.import-static))

(def snake (ref {}))
(def apple (ref {}))
(def direction (ref {}))
(def steps (ref {}))
(def score (ref {}))

(def WIDTH "Width of the game board" 19)
(def HEIGHT "Height of the game board" 10)
(def LEFT "Left direction" [-1 0])
(def RIGHT "Right direction" [1 0])
(def UP "Up direction" [0 -1])
(def DOWN "Down direction" [0 1])
(def RIGHT-TURN "Right turn vector" [1 -1])
(def LEFT-TURN "Left turn vector" [-1 1])

(defn add-points
  "Add vector points."
  [& pts]
  (vec (apply map + pts)))

(defn eats?
  "Check if the snake eats an apple."
  [{[snake-head] :body} {apple :location}]
  (= snake-head apple))

(defn change-direction
  "Change direction of the snake."
  [old-dir turn]
  (vec (reverse (map * old-dir turn))))

(defn move
  "Move the snake in a given direction."
  [{:keys [body score] :as snake} dir apple & grow]
  (assoc snake :body (cons (add-points (first body) dir)
                           (if grow body (butlast body)))
               :score (if grow (inc score) score)))

(defn create-apple
  "Create an apple."
  []
  {:location [(rand-int WIDTH) (rand-int HEIGHT)]
   :color (Color. 210 50 90)
   :type :apple})

(defn create-snake
  "Create the snake."
  []
  {:body (for [x (range 8 -1 -1)] [x 10])
   :type :snake
   :color (Color. 15 160 70)
   :score 0})

;;;
;;; GP terminals
;;;
(defn turn-right
  "Make the snake turn right."
  []
  (set! direction (change-direction direction RIGHT-TURN))
  (set! snake (move snake direction apple))
  (set! steps (inc steps))
  (if (eats? snake apple)
    (do
      (set! apple (create-apple))
      (set! score (inc score)))))

(defn turn-left
  "Make the snake turn left."
  []
  (set! direction (change-direction direction LEFT-TURN))
  (set! snake (move snake direction apple))
  (set! steps (inc steps))
  (if (eats? snake apple)
    (do
      (set! apple (create-apple))
      (set! score (inc score)))))

(defn move-forward
  "Make to snake continue forward."
  []
  (set! snake (move snake direction apple))
  (set! steps (inc steps))
  (if (eats? snake apple)
    (do
      (set! apple (create-apple))
      (set! score (inc score)))))

;;;
;;; GP functions
;;;
;;; (initial function set)
;;;
(defmacro if-food-ahead
  "GP food ahead macro."
  [food no-food]
  `(if (food-ahead? snake direction apple)
     ~food
     ~no-food))

(defmacro if-danger-ahead
  "GP danger ahead macro."
  [danger no-danger]
  `(if (danger-ahead? snake direction)
     ~danger
     ~no-danger))

(defmacro if-danger-right
  "GP danger right macro."
  [danger-right no-danger-right]
  `(if (danger-right? snake direction)
     ~danger-right
     ~no-danger-right))

(defmacro if-danger-left
  "GP danger left macro."
  [danger-left no-danger-left]
  `(if (danger-left? snake direction)
     ~danger-left
     ~no-danger-left))

;;; function (do exprs*) is used as progn2

;;;
;;; GP functions
;;;
;;; (full function set)
;;;
(defmacro if-danger-two-ahead
  "GP danger two ahead macro."
  [danger-two-ahead no-danger-two-ahead]
  `(if (danger-two-ahead? snake direction)
     ~danger-two-ahead
     ~no-danger-two-ahead))

(defmacro if-food-up
  "GP food up macro."
  [food-up no-food-up]
  `(if (food-up? snake apple)
     ~food-up
     ~no-food-up))

(defmacro if-food-right
  "GP food right macro."
  [food-right no-food-right]
  `(if (food-right? snake apple)
     ~food-right
     ~no-food-right))

(defmacro if-moving-right
  "GP moving right macro."
  [moving-right no-moving-right]
  `(if (moving-right? direction)
     ~moving-right
     ~no-moving-right))

(defmacro if-moving-left
  "GP moving left macro."
  [moving-left no-moving-left]
  `(if (moving-left? direction)
     ~moving-left
     ~no-moving-left))

(defmacro if-moving-up
  "GP moving up macro."
  [moving-up no-moving-up]
  `(if (moving-up? direction)
     ~moving-up
     ~no-moving-up))

(defmacro if-moving-down
  "GP moving down macro."
  [moving-down no-moving-down]
  `(if (moving-down? direction)
     ~moving-down
     ~no-moving-down))
