(ns clojure-snake.gpset
  (:import (java.awt Color)))

(def snake (ref {}))
(def apple (ref {}))
(def direction (ref {}))
(def steps (ref {}))
(def score (ref {}))

(def WIDTH "Width of the game board" 50)
(def HEIGHT "Height of the game board" 30)
(def LEFT "Left direction" [-1 0])
(def RIGHT "Right direction" [1 0])
(def UP "Up direction" [0 -1])
(def DOWN "Down direction" [0 1])

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

(defn calc-new-head
  "Add vector points."
  [& pts]
  (vec (apply map + pts)))

(defn eats?
  "Check if the snake eats an apple."
  [{[snake-head] :body} {apple :location}]
  (= snake-head apple))

(defn move
  "Move the snake in a given direction."
  [{:keys [body] :as snake} dir apple-loc]
  (assoc snake :body (cons (calc-new-head (first body) dir)
                           (if (eats? snake apple-loc)
                             (do
                               (ref-set apple (create-apple))
                               (ref-set score (inc @score))
                               body)
                             (butlast body)))))

(defn out-of-bounds?
  "Check if the snake is out of bounds (wall hit)."
  [{[head] :body}]
  (or (< (head 0) 0)
      (> (head 0) WIDTH)
      (< (head 1) 0)
      (> (head 1) HEIGHT)))

(defn head-overlaps-body?
  "Check if the snake has collided with itself."
  [{[head & body] :body}]
  (contains? (set body) head))

(defn game-over? [snake]
  (or (head-overlaps-body? snake) (out-of-bounds? snake)))
