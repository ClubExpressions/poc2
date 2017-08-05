# Second (and last) POC for the Club des Expressions

See the running app here:  
<https://clubexpressions.github.io/poc2/resources/public/>

## Technical stack

### Overview

* We build an [SPA](https://en.wikipedia.org/wiki/Single-page_application)
  (see [this article](https://johnpapa.net/pageinspa/) for a demythification)
* using [re-frame](https://github.com/Day8/re-frame/) which is a
  * [ClojureScript](https://clojurescript.org/), which is
    * [Clojure](https://clojure.org/) (a clever
      [Lisp](https://en.wikipedia.org/wiki/Lisp_(programming_language))
      on the [JVM](https://en.wikipedia.org/wiki/Java_virtual_machine))
    * that compiles to [JavaScript](https://en.wikipedia.org/wiki/JavaScript)
      (the language animating browsers)
  * [framework](https://en.wikipedia.org/wiki/Software_framework) built around
  * [Reagent](https://github.com/reagent-project/reagent) which is a
    ClojureScript interface to
    * [React](https://facebook.github.io/react/) using the
    * [Hiccup](https://github.com/weavejester/hiccup)-like syntax.
  * We have the powers of all the packages available at
    [npm](http://npmjs.com/), a huge software repository for JavaScript.
* Authentication thanks to [Auth0](http://auth0.com/).
* Persistency thanks to [Kinto](kinto.readthedocs.io/).

[Figwheel](https://github.com/bhauman/lein-figwheel) allows us to live code.

### Details and POC TODOs

* Working integration of npm packages in ClojureScript (done
  [this way](http://blob.tomerweller.com/reagent-import-react-components-from-npm)
  but [there may be a cleaner way](https://clojurescript.org/news/2017-07-12-clojurescript-is-not-an-island-integrating-node-modules))
  * [react-mathjax](https://www.npmjs.com/package/react-mathjax)
    (math typesetting in the browser)
  * [clubexpr](https://www.npmjs.com/package/clubexpr) (math expressions)
  * [Kinto](https://www.npmjs.com/package/kinto) (persistency)
  * [Auth0](https://www.npmjs.com/package/auth0) (authentication)
  * [React-Bootstrap](https://www.npmjs.com/package/react-bootstrap)
    instead of just the `bootstrap` package (nice UI in the browser)
* Modules that caused problems (there is a branch for the attempt)
  * [CodeMirror](https://www.npmjs.com/package/react-codemirror) (text editor)
* Modules that are planned to be used:
  * [Tempura](https://github.com/ptaoussanis/tempura) (i18n)

## Dev setup

### Leiningen (the command is lein)

Install the Clojure/ClojureScript package manager, build frontend…

Install as told at [leiningen.org/](https://leiningen.org/). Yes, it a single
script which, when run, will install everything it needs.

It reads the `project.clj` lying at the root dir of the project.

### Get the source

`git clone git@github.com:ClubExpressions/poc2.git`

### Dev build and source watch

1. `cd` to the root of this project (where this README exists)
2. run `lein do clean, figwheel`  to compile the app and start up
   figwheel hot-reloading,
3. open `http://localhost:3449/` to see the app

While step 2 is running, any changes you make to the ClojureScript
source files (in `src`) will be re-compiled and reflected in the running
page immediately.

### kinto

Client side, we use the official npm package
[kinto](https://www.npmjs.com/package/kinto) (instead of the other official
[kinto-http](https://www.npmjs.com/package/kinto-http)).

The dev build points to <http://localhost:8887/v1> and the prod build to
<https://kinto.dev.mozaws.net/v1> (thanks to

    :closure-defines {goog.DEBUG false}

in `project.clj` whose value is in turn stored in the `debug?` var).

Our own instance will be live
[soonish](https://github.com/Kinto/kinto-alwaysdata/issues/created_by/grahack).
Some notes taken below.

#### General instructions to install the Kinto server

* `sudo apt-get install python3-dev`
* `sudo pip3 install kinto`
* for use with PG :
  * `sudo apt-get install postgresql`
  * `sudo pip3 install psycopg2 SQLAlchemy zope.sqlalchemy`

#### Local kinto

There are some `kinto.ini` files in the repo, just do  
`kinto start --ini kinto_XXX.ini`.

#### Official test instance

* Everything works ok with Firefox.
* /!\ Our Github page use https, the kinto test instance too, but Chrome
may complain about security issues.

#### Prod kinto

Attempts to use [kinto-alwaysdata](https://github.com/kinto/kinto-alwaysdata),
but encountered
[issues](https://github.com/Kinto/kinto-alwaysdata/issues/created_by/grahack).

* `mkdir where-kinto-conf-will-be`
* `cd !$`
* `kinto init`
* PG is running
* `kinto migrate`

#### Kinto admin (like phpMyAdmin)

[Project page](https://github.com/Kinto/kinto-admin)

* first instructions (create a React app)
  [don't work](https://github.com/Kinto/kinto-admin/issues/446)
* I just did:
  * `git clone https://github.com/Kinto/kinto-admin.git`
  * `cd kinto-admin`
  * `npm install`
  * `npm start`
  * then had a look at <http://localhost:3000>.

## Dev notes

### Using an npm module in the ClojureScript build

The POC was done with
[react-mathjax](https://www.npmjs.com/package/react-mathjax).

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

## Prod build

The idea is to build a prod version (`:prod` in `project.clj`) and
push the new `client.js` to the Github pages. All other maybe useless files
(.cljs) will be available too on the Github pages.

You can just rebase and push like:

    $ # kill lein dev
    $ lein do clean, with-profile prod compile
    $ # check at /path_to_prj/resources/public/index.html` in a browser.
    $ git co gh-pages
    $ git co .  # may be needed because client.js was ignored
    $ git rebase master
    Switched to branch 'gh-pages'
    $ lein do clean, with-profile prod compile  # again
    ...
    $ git add -u
    $ git commit --amend -C HEAD
    First, rewinding head to replay your work on top of it...
    Applying: Add resources/public/js/client.js
    $ git push origin gh-pages -f
    ...
    $ git co master

Then see it live here:  
<https://clubexpressions.github.io/poc2/resources/public/>
