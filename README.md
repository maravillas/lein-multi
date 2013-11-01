# lein-multi

lein-multi is a Leiningen plugin for running tasks against multiple dependency sets. Most notably, it allows you to test your project against multiple versions of Clojure.

**NOTE:** This plugin has been superseded by [Leiningen 2.x's profiles](https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md).

## Usage

Syntax:

`$ lein multi <task> [--with <set name>] [args]`

Specify dependency sets in your project.clj:

    :multi-deps {"1.1" [[org.clojure/clojure "1.1.0"]
              	        [ring/ring-core "0.2.2"]]
                 "1.2" [[org.clojure/clojure "1.2.0"]
                        [ring/ring-core "0.3.0-beta1"]]
                 :all  [[compojure "0.6.4"]]}

Download these dependencies with `lein multi deps` (or by running your tests with `lein multi test`). They will be placed in the folder specified by `:multi-library-path` (default: `multi-lib`).

Dependencies included in the :all set will be included with all other sets, including the base set specified in the :dependencies keyword in root of your project.clj.

Run a task against a single dependency set from :multi-deps using the `--with` option followed by the set name as specified in your project.clj.

lein-multi will accept and run other tasks, though the result may not be what you expect. Tasks that do not require a project (currently `new`, `help`, and `version`) are run as normal. 

Additionally, other tasks may require special handling that isn't yet implemented. For example, `lein multi uberjar` will only create one jar.

## Installation

`lein plugin install lein-multi 1.1.0`

## TODO

* Special handling for pom, jar, uberjar, and clean
* Final test summary for all sets

## License

Copyright (c) 2010 Matthew Maravillas

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
