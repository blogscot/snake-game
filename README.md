# clojure-snake

clojure-snake is a simple implementation of the snake game written in Clojure and Swing. In this implementation the game board size is 20 squares wide and 11 squares high.The snake is coloured green and has 9 body squares and starts at position (1,11)-(9,11) moving to the right. Food pieces (apples) are coloured red and pop up at random positions. By eating apples, snake grows one body square at a time. The maximum number of apples to eat is 211, at which point the snake fill the whole game board. In order to survive the player must control the snake in order to avoid the wall and its own body while trying to collect as much apples as possible. The projects also has an AI mode which enables the player to load the output from [snake-pit](https://github.com/somi92/snake-pit) project and see the AI in action.

The project is inspired by the following [code](https://github.com/stuarthalloway/programming-clojure/blob/master/src/examples/snake.clj) from the book "Programming Clojure" by Stuart Halloway and Aaron Bedra. 

## Installation and usage

You must have Java installed on your machine. Download the jar file that can be found in the releases section of this respository. Run the following command in command line to start the game:

`java -jar clojure-snake-0.1.0-standalone.jar -r <routine_argument> -s <speed_integer>`

The first parameter `<routine_argument>` determines the control routine from [snake-pit](https://github.com/somi92/snake-pit) project if you want to use the AI. The second parameter `<speed_integer>` determines the snake's speed and it should be an integer between 0 (slowest) and 50 (fastest). For example: 
```
java -jar clojure-snake-0.1.0-standalone.jar -r "(if-food-ahead (move-forward) (if-food-right (move-forward) (turn-left)))" -s 40
```

If you want manual play pass the empty string as first argument:
```
java -jar clojure-snake-0.1.0-standalone.jar -r "" -s 25
```

The project can also be used as a library and all you need to do is to include the jar file into the classpath of your project. Then call the `game` function with parameters as explained above. For example:

```
(use 'clojure-snake.core)
(game "(if-food-ahead (move-forward) (if-food-right (move-forward) (turn-left)))" 40)
; or manual play
(game "" 25)
```

## Bugs

The project contains a bug regarding the drawing of the snake while moving on the board. The snake seems to skip positions while moving, it doesn't go square by square as expected. But when the player turns the snake and holds down the direction key it starts to behave normally and moves square by square, as soon as the key is released it starts skipping again.

Check out the [question on Stack Overflow](http://stackoverflow.com/questions/30551977/clojure-snake-skips-positions) for more details.

## Related projects

As mentioned above, this game can handle the output of the snake-pit application which is represented as a string of nested function calls. For more details on how to create the snake game AI check out the [snake-pit](https://github.com/somi92/snake-pit) project repository.

Also, for full convenience there is the [snake-pit-ui](https://github.com/somi92/snake-pit-ui) project written in Java which wraps both snake-pit and clojure-snake with a nice UI with all the options available. It enables easy and fast experimenting with GP settings, testing the results as well as manual play.

## Contact

If you have some comments, suggestions or noticed some bugs and problems feel free to contact me and contribute.

Milos Stojanovic email: stojanovicmilos31@gmail.com

## License

Copyright © 2015

Distributed under the Eclipse Public License. The copy of the license is available in the repository.
