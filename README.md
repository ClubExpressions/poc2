# POC 2 pour le Club des Expressions

## Installation

* kinto
  * `sudo apt-get install python3-dev`
  * `sudo pip3 install kinto`

## Dev notes

### Base files from the re-frame tutorial

*  project.clj
*  resources/public/example.css
*  resources/public/example.html
*  src/simple/core.cljs

## Run

### Dev

1. `cd` to the root of this project (where this README exists)
2. run "`lein do clean, figwheel`"  to compile the app and start up
   figwheel hot-reloading, 
3. open `http://localhost:3449/example.html` to see the app

While step 2 is running, any changes you make to the ClojureScript 
source files (in `src`) will be re-compiled and reflected in the running 
page immediately.

### Prod

Run `lein do clean, with-profile prod compile` to compile an optimised 
version, and then open `resources/public/example.html` in a browser.
