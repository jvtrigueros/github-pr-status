(defproject github-pr-status "0.1.0-SNAPSHOT"
  :description "A program that will poll a GitHub repo, look for new pull requests, and tweet a summary of each PR to Twitter"
  :url "https://gitlab.com/jvtrigueros/github-pr-status"
  :license "UNLICENSED"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.3.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]]
                   :source-paths ["dev"]}})
