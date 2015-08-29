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

![Playing](https://raw.githubusercontent.com/somi92/clojure-snake/master/resources/snake1.png?raw=true "Playing")

## Bugs

The project contains a bug regarding the drawing of the snake while moving on the board. The snake seems to skip positions while moving, it doesn't go square by square as expected. But when the player turns the snake and holds down the direction key it starts to behave normally and moves square by square, as soon as the key is released it starts skipping again.

Check out the [question on Stack Overflow](http://stackoverflow.com/questions/30551977/clojure-snake-skips-positions) for more details.

## Different implementations

This section will refer to some other implementations of the snake game found on the web and describe them briefly, focusing mainly on the differences.

One implementation by Mark Volkmann can be found [here](http://java.ociweb.com/mark/programming/ClojureSnake.html). It is based on Swing and by that shares some similarities with the clojure-snake project. Unlike the clojure-snake project which is using mutable refs to hold the game state, this implementation uses only one atom called `key-code-atom` to hold the key code of the latest key press. This atom is updated on the key pressed event and used to update the snakes position. The project has some structs defining the basic game parameters. There is a struct for defining a point in space called `cell-struct`, a struct for defining a snake called `snake-struct` and `game-struct` that holds all game elements and parameters. These structs are used by the game functions to initialize, handle and update the game state. Instead of repainting the whole panel, snake and apple, Volkmann's implementation paints and erases individual cells on the game board as needed. It has functions for painting and erasing individual cells, `paint-cell` and `erase-cell`. These functions are used by other functions that handle snake and apple movement. The clojure-snake project uses Swing timer as a game loop which updates the game's state, while this project uses a standard Clojure loop paused by the Threed sleep method. Inside the loop, painting function is called and on recur the `step` function. This function checks if the apple is eaten, updates the snake and game structs, moves the snake and checks for the end of the game. Volkmann's implementation uses different logic for apple eating. The snake doesn't need to collide its head directly with the apple but can eat it when located in the adjacent cells not more than one step far. When faced with the wall, the snake makes an automated clockwise turn to avoid death. When death occurs, the game is automatically restarted with a snake one cell of size. To start the game the `main` function is used. It initializes all the elements and contains the game loop. This project also suffers from the same bug as described above.

Another very interesting and lean implementation is [this](http://fn-code.blogspot.com/2015/08/a-clojure-snake-game.html) by Mark Bastian. It has only 75 lines of code and uses [Quil](http://quil.info/) library for creating interactive drawings and animations. The game board is defined in `world` var and there are functions to handle the snake and its movement, food pieces, setup, etc. The `draw` function uses Quil API to draw the snake, background, food pieces and display score information. The `setup` function is used to initialize the frame rate and game elements. To start the game `launch-sketch` is used. It also uses Quil API to setup all parameters, including the setup and draw functions and key press listener. To update the state anonymous function `#(-> % grow-snake eat (update :food replenish-food (world :food-amount)) reset?)` is used. It chains function calls in defined order passing the state from one function to another. It is a more convenient way of nesting functions. This is known as "threading" in Clojure. Unlike clojure-snake, this implementation doesn't place one food piece at a time, but keeps a constant number of 1000 pieces at the game board which is 100x100 points in size. When the snake reaches the wall it wraps around to the opposite side. The Quil library makes this implementation very lean and interesting because its easier to draw the game. The project is written in Clojure and can also target ClojureScript which is a compiler for Clojure that targets JavaScript. So, the project can be run as a traditional Clojure application, as well as JavaScript application suitable for web pages. For more details check out the [article](http://fn-code.blogspot.com/2015/08/a-clojure-snake-game.html) and [GitHub repository](https://github.com/markbastian/snake).

## Related projects

As mentioned above, this game can handle the output of the snake-pit application which is represented as a string of nested function calls. For more details on how to create the snake game AI check out the [snake-pit](https://github.com/somi92/snake-pit) project repository.

Also, for full convenience there is the [snake-pit-ui](https://github.com/somi92/snake-pit-ui) project written in Java which wraps both snake-pit and clojure-snake with a nice UI with all the options available. It enables easy and fast experimenting with GP settings, testing the results as well as manual play.

## Contact

If you have some comments, suggestions or noticed some bugs and problems feel free to contact me and contribute.

Developed by Milos Stojanovic 

email: stojanovicmilos31@gmail.com

## License

Copyright © 2015

Distributed under the Eclipse Public License. The copy of the license is available in the repository.
