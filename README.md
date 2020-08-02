# FLLControlPanel

This Application creates many customizable Info-Screens designed for [FLL](https://www.first-lego-league.org/en/challenge/home.html) Tournaments.

## Features

- Data Import from commonly used Tournament-Configurator by Nano-Giants
- Reads Scoreboard-Data from the official [HoT-System](https://et.hands-on-technology.de)
- Integration for [OBS](https://obsproject.com/) using [OBS-Websocket](https://obsproject.com/forum/resources/obs-websocket-remote-control-obs-studio-from-websockets.466/) [Work in Progress]
- Music-Integration for [FooBar2000](https://www.foobar2000.org/) with Telnet. [Work in Progress]
- Changeable screen-configuration to fit the needs of every Tournament.

## Screens

- Greetings (shown automatically)
- Timer (/timer)
- Clock (/clock)
- Scoreboard (/scoreboard)
- Timetable (/timetable)
- Jury-Information [all Juries] (/jury)
- Jury-Information [one Jury] (/room?room={juryIdentifier} with e.g. R3 for Robot-Design, T1 for Teamwork, F2 for Research)
- Stream-Overlay [planed]
- Gantt chart [planed]

<!-- ## Screenshots -->
  

## Usage

You can download the latest .jar-File [here](https://github.com/Strohgelaender/FLLControlPanel/releases). 
You need Java 14 or higher to execute the application.

You can start the application by either double-clicking the file or by typing `java -jar FLLControlPanel.jar` to the command-line. 

After Importing the data you can access the screens via the browser. 
The default Port of the webserver is `8080`. You can change it by adding the argument `-Dserver.port=YourNewPort` at the command-line.

If you've started the Application on Port `8080` and your local IP is `192.168.2.103` then e.g. the timer can be found at `192.168.2.103:8080/timer`.

## Participate

If you have a feature-suggestion or found a bug, please open a [issue](https://github.com/Strohgelaender/FLLControlPanel/issues).
Also feel free to change code and open a [pull request](https://github.com/Strohgelaender/FLLControlPanel/pulls).

