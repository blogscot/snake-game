(ns clojure-snake.run-snake
  (:use clojure-snake.core)
  (:use clojure-snake.gpset))

(def rtn
  "(if-danger-ahead
 (if-danger-ahead
  (if-danger-two-ahead
   (if-danger-left (turn-right) (turn-left))
   (turn-right))
  (if-food-up
   (if-food-right (move-forward) (move-forward))
   (if-danger-two-ahead
    (if-danger-left (turn-right) (turn-left))
    (turn-left))))
 (if-danger-right
  (if-moving-up
   (if-moving-up
    (if-food-up
     (if-food-right (move-forward) (move-forward))
     (if-danger-right (turn-left) (turn-right)))
    (if-danger-ahead
     (if-food-up (move-forward) (turn-right))
     (if-moving-right (turn-right) (turn-right))))
   (if-food-ahead (move-forward) (move-forward)))
  (if-moving-down
   (turn-left)
   (if-food-right
    (if-moving-right
     (if-food-up (move-forward) (turn-left))
     (if-moving-down (move-forward) (turn-right)))
    (if-danger-ahead
     (if-moving-right (move-forward) (turn-right))
     (if-moving-right (turn-left) (move-forward)))))))")

(game nil 50)

