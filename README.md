# Parade
Cloned From: https://github.com/yquanjun/CS102
## Introduction
Parade is a card game where the aim is to get the lowest score possible from the cards collected. Players will play the game on their console. Our application supports both single-player and multi-player game modes. For single-player mode, players will play against bots locally. For multi-player mode, players can play with each other through a Socket connection. Bots can optionally be added to a multi-player game. For more instructions on the rules of the game, refer to https://cdn.1j1ju.com/medias/8f/7e/8f-parade-rulebook.pdf. 

## Installation and Set-Up
To install, download the zip file or run ```git clone https://github.com/yquanjun/CS102.git```. After installation, open terminal at the top level directory of the app (```.../CS102```). 

For Mac/Linux users, run the command ```chmod +x compile.bat run.bat``` before proceeding on to the next steps. 

In order to play multi-player mode, all devices must:
1. Be in the same network
2. Have firewall deactivated

## How to Use
To start the app, run the following command:
- For Windows: ```compile.bat && run.bat```
- For Mac/Linux: ```./compile.bat && ./run.bat```

Next, enter 'Yes' if you understand the Parade rules, and 'No' otherwise. A link to the official Parade instructions will be provided. 

After this, you will be prompted to decide whether you want to start a new game or join a server. If you are joining an existing game (i.e. someone has already started the game), choose to join the game. Otherwise, you may start a new game. Following this, you will be prompted to select if you want to play in single-player mode or multi-player mode. 

If you have selected multi-player mode, the rules for the special BlackJack game mode will be displayed. You will also be prompted if you want to play with the BlackJack rules or not. BlackJack mode is only available for multi-player mode, and it does not support bots. 

After you key in the number of players you wish to play, the game can begin! If you are playing in multi-player mode, a server will start. You will need to use another terminal to join the game. The game will only start when the number of expected players have joined the game. 

## Project Structure
```
CS102/
├── doc/
│   └── // Javadoc here
│   └── index.html
│
├──src/
│  └── app/
│  │   └── board/
│  │   │   └── Card.java
│  │   │   └── Deck.java
│  │   │   └── ParadeLine.java
│  │   |
│  │   └── user/
│  │   │   └── ComputerPlayer.java
│  │   │   └── HumanPlayer.java
│  │   │   └── Player.java
│  │   │
│  │   └── BlackJack.java
│  │   └── GameController.java
│  │   └── MultiplayerController.java
│  │   └── ParadeApp.java
│  │   └── SinglePlayerController.java
│  │  
│  └──  net/
│  │    └── Client.java
│  │    └── ClientHandler.java
│  │    └── Server.java
│  │   
│  └── utility/
│  │   └── AsciiArt.java
│  │   └── Colour.java
│  │   └── ConsoleController.java
│  │   └── Pair.java
│  │   └── Printer.java
│  │
│  └── compile.bat
│  └── config.properties
│  └── README.md
│  └── run.bat
```

## Team Members
1. Lo Wei Shen 
2. Yang Quanjun
3. Lau Wei Bin
4. Amos Young Zhi Kai
5. Liu Jiajun
6. Joseph Yau Jit Sian
