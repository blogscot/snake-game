(ns clojure-snake.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class)
  (:import (java.awt Graphics Color Dimension)
           (java.awt.event ActionListener KeyListener KeyEvent)
           (java.io FileInputStream BufferedInputStream)
           (javax.swing JPanel JFrame Timer JOptionPane WindowConstants)))

(def snake (ref {}))
(def apple (ref {}))
(def direction (ref {}))
(def score (ref {}))
(def pause? (ref false))
(def game-timer (ref {}))
(def game-highscore (ref 0))

(def WIDTH "Width of the game board" 50)
(def HEIGHT "Height of the game board" 30)
(def LEFT "Left direction" [-1 0])
(def RIGHT "Right direction" [1 0])
(def UP "Up direction" [0 -1])
(def DOWN "Down direction" [0 1])
(def GREEN (Color. 15 160 70))
(def RED (Color. 210 50 90))
(def BLACK (Color. 0 0 0))
(def GOLD (Color. 225 185 0))

; constants to describe time, space and motion
(def point-size 15)
(def turn-millis 600)
(def win-length 100)
(def dirs {KeyEvent/VK_LEFT LEFT
           KeyEvent/VK_RIGHT RIGHT
           KeyEvent/VK_UP UP
           KeyEvent/VK_DOWN DOWN})

(defn create-apple
  "Create an apple."
  []
  {:location [(rand-int WIDTH) (rand-int HEIGHT)]
   :color RED
   :type :apple})

(defn create-snake
  "Create the snake."
  []
  {:body (for [x (range 8 -1 -1)] [x 10])
   :type :snake
   :color GREEN})

(def highscore-filename "hscore.txt")

(defn load-highscore []
  (if (.exists (io/file highscore-filename))
    (read-string (slurp highscore-filename))
    0))

(defn save-highscore [score]
  (when (> score (load-highscore))
    (spit highscore-filename score)))

(defn reset-game "resets the game state and timer."
  [snake apple]
  (let [{:keys [timer initial]} @game-timer]
    (dosync (ref-set snake (create-snake))
            (ref-set apple (create-apple))
            (ref-set direction RIGHT)
            (ref-set score 0)
            (ref-set pause? false)
            (ref-set game-timer (assoc @game-timer :period initial))
            (ref-set game-highscore (load-highscore)))
    (.setDelay timer initial)))

(defn calc-new-head
  "Returns the position of the new head"
  [& pts]
  (vec (apply map + pts)))

(defn eats?
  "Check if the snake eats an apple."
  [{[snake-head] :body} {apple :location}]
  (= snake-head apple))

(defn increase-speed []
  (let [{:keys [timer period]} @game-timer
        new-period (int (* period 0.98))]
    (dosync (ref-set game-timer (assoc @game-timer :period new-period)))
    (.setDelay timer new-period)))

(defn play-file [filename]
  (let [fstream (FileInputStream. filename)
        bstream (BufferedInputStream. fstream)
        player (javazoom.jl.player.Player. bstream)]
    (.start (Thread. #(doto player (.play) (.close))))))

(defn calc-new-score
  "Returns the new score. Scoring increases in proportion to current game speed."
  [score]
  (let [{:keys [period initial]} @game-timer
        delta (int (inc (/ (- initial period) 10)))]
    (+ score (* 10 delta))))

(defn move
  "Move the snake in a given direction."
  [{:keys [body] :as snake} dir apple-loc]
  (assoc snake :body (cons (calc-new-head (first body) dir)
                           (if (eats? snake apple-loc)
                             (do
                               (increase-speed)
                               (ref-set apple (create-apple))
                               (ref-set score (calc-new-score @score))
                               (play-file "resources/drop.mp3")
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

(defn point-to-screen-rect
  "Returns the origin x, y plus dx and dy needed
   to draw a square of size point."
  [[x y]] (map #(* point-size %) [x y 1 1]))

(defn player-win? [{body :body}] (>= (count body) win-length))

(defn update-snake-direction
  "Updates the direction of the snake. Prevents reversing
   head into body which would terminate game."
  [newdir]
  (let [reverse-dir? (fn [dir] (= dir (map #(* -1 %) @direction)))]
    (when (not (reverse-dir? newdir))
      (dosync (ref-set direction newdir)))))

; function for updating positions of snake and apple
(defn update-positions [snake apple]
  (dosync
   (alter snake move @direction @apple))
  nil)

; ---------------------------------------------------------------------
; gui
; ---------------------------------------------------------------------
; function for making a point on the screen
(defn fill-point [^Graphics g pt ^Color color]
  (let [[^int x ^int y ^int width ^int height] (point-to-screen-rect pt)]
    (.setColor g color)
    (.fillRect g x y width height)))

(defn display-score
  "Displays the latest score and game highscore.
   Also modifies the snake colour if the highscore is reached."
  [^Graphics g score]
  (let [highscore @game-highscore
        ^String score-text (str "SCORE " score "  HIGH SCORE " highscore)]
    (when (>= score @game-highscore)
      (dosync
       (ref-set snake (assoc @snake :color GOLD))))
    (.setColor g BLACK)
    (.drawString g score-text 305 20)))

; function for painting snakes and apples
(defmulti paint (fn [_g object & _] (:type object)))

(defmethod paint :apple [^Graphics g {:keys [location color]}]
  (fill-point g location color)
  (display-score g @score))

(defmethod paint :snake [g {:keys [body color]}]
  (doseq [point body]
    (fill-point g point color)))

(defn prompt-play-again [frame e msg]
  (if (= JOptionPane/YES_OPTION
         (JOptionPane/showConfirmDialog
          frame (str "Apples eaten: " @score "\n Play again?")
          msg
          JOptionPane/YES_NO_OPTION))
    (reset-game snake apple)
    (do
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
        (update-positions snake apple))
      (when (game-over? @snake)
        (save-highscore @score)
        (prompt-play-again frame e "Game Over!"))
      (when (player-win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "You win!"))
      (when (some? this)
        (.repaint this)))
    (keyPressed [e]
      (let [keycode (.getKeyCode e)]
        (cond
          (or (= keycode KeyEvent/VK_LEFT)
              (= keycode KeyEvent/VK_RIGHT)
              (= keycode KeyEvent/VK_UP)
              (= keycode KeyEvent/VK_DOWN)) (update-snake-direction (dirs keycode))
          (= keycode KeyEvent/VK_ESCAPE) (System/exit 0)
          (= keycode KeyEvent/VK_P) (dosync
                                     (ref-set pause? (not @pause?))))))
    (getPreferredSize []
      (Dimension. (* (inc WIDTH) point-size)
                  (* (inc HEIGHT) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

(defn game [speed]
  (dosync
   (ref-set snake (create-snake))
   (ref-set apple (create-apple))
   (ref-set direction RIGHT)
   (ref-set score 0)
   (ref-set game-highscore (load-highscore))
   (let [frame (JFrame. "Snake Game - (press ESCAPE to exit, P to toggle pause)")
         panel (game-panel frame snake apple)
         period (- turn-millis (* 10 speed))
         timer (Timer. period panel)]
     (doto panel
       (.setFocusable true)
       (.addKeyListener panel))
     (doto frame
       (.add panel)
       (.pack)
       (.setVisible true)
       (.setResizable false)
       (.setDefaultCloseOperation WindowConstants/EXIT_ON_CLOSE))
     (ref-set game-timer {:timer timer :period period :initial period})
     (.start timer))))

(def cli-options
  ;; An option with a required argument
  [["-s" "--speed SPEED" "Snake speed. Values 0 (Easy) - 40 (Hard)"
    :default 20
    :id :speed
    :parse-fn #(Integer/parseInt %)
    :validate [#(<= 0 % 40) "Must be a number between 0 and 40"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["The Snake Game."
        ""
        "Guide your hungry snake around the playing field eating"
        "apples by using the cursor keys."
        ""
        "Usage: snake-game [options]"
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn -main
  [& args]
  (let [{:keys [options summary errors]} (parse-opts args cli-options)
        {:keys [speed help]} options]
    (if (or (seq errors) help)
      (println (usage summary))
      (game speed))))
