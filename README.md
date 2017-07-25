# POC 2 pour le Club des Expressions

See the running app here:  
<https://clubexpressions.github.io/poc2/resources/public/>

## Installation

### kinto

* `sudo apt-get install python3-dev`
* `sudo pip3 install kinto`
* for use with PG :
  * `sudo apt-get install postgresql`
  * `sudo pip3 install psycopg2 SQLAlchemy zope.sqlalchemy`
* `mkdir where-kinto-conf-will-be`
* `cd !$`
* `kinto init`

#### kinto in memory

* `kinto migrate`

## Dev notes

### Base files from the re-frame tutorial

*  project.clj
*  resources/public/example.css
*  resources/public/index.html (was example.html)
*  src/simple/core.cljs

### Using react-mathjax

From [this blog post](http://blob.tomerweller.com/reagent-import-react-components-from-npm),
where you'll find where I got the content of `package.json`,
`webpack.config.js` and `src/js/main.js`.

    $ vim package.json  # or use https://github.com/RyanMcG/lein-npm ?
    $ npm install
    $ vim webpack.config.js

I attempted to add `resources` before `public/js` but `lein clean` deleted
`bundle.js`! Running `npm run build` after `lein clean` was not good either
(can't remember why).

    $ mkdir src/js
    $ vim src/js/main.js  # remember to change player -> mathjax or whatever
    $ sudo npm install -g webpack
    $ npm run build
    $ vim project.clj  # to add exclusions of reagent
    $ vim project.clj  # and add the libs we are trying to use
                       # see https://clojurescript.org/reference/compiler-options
                       # for hints about the correct position of :foreign-libs
    $ lein clean && lein figwheel
    $ vim src/truc/core.cljs  # beware, there's a typo, use 'r' not 'reagent'

The commit of this addition in the README should be `POC mathjax` and also
contains all the relevant changes.

### Simple steps for adding another node package thereafter

    $ vim package.json  # just add one line
    $ npm install
    $ vim src/js/main.js  # add one line
    $ npm run build
    $ lein clean && lein figwheel
    $ vim src/truc/core.cljs  # require and use your package or component

### Simple steps for updating a node package

    $ vim package.json  # just add one line
    $ npm install
    $ npm run build
    $ git add package.json public/js/bundle.js
    $ git commit -m "Update name_of_the_package version_number"
    $ stop figwheel
    $ lein clean && lein figwheel  # maybe hard refresh to be sure
    $ use the new version

## Run

### Dev

1. `cd` to the root of this project (where this README exists)
2. run "`lein do clean, figwheel`"  to compile the app and start up
   figwheel hot-reloading, 
3. open `http://localhost:3449/` to see the app

While step 2 is running, any changes you make to the ClojureScript 
source files (in `src`) will be re-compiled and reflected in the running 
page immediately.

### Prod

Run `lein do clean, with-profile prod compile` to compile an optimised 
version, and then open `resources/public/example.html` in a browser.

Update `resources/public/js/client.js` in the `gh-pages` branch (you'll have
to force the `git add` with `-f` since `resources/public/js/` is gitignored).

You can just rebase and push like:

    $ git co gh-pages
    $ git co .  # this is because client.js was ignored
    $ git rebase master
    Switched to branch 'gh-pages'
    $ lein do clean, with-profile prod compile
    ...
    $ git add resources/public/js/client.js  # -f the first time only
    $ git commit --amend -C HEAD
    First, rewinding head to replay your work on top of it...
    Applying: Add resources/public/js/client.js
    $ git push origin gh-pages -f
    ...
    $ git co master

Then see it live here:  
<https://clubexpressions.github.io/poc2/resources/public/>
