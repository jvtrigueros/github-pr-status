(ns github.pr-test
  (:require [clojure.test :refer [deftest is]]
            [github.graphql :as gh-graphql]
            [clojure.string :as str]))

(def config
  {:token      (System/getenv "GITHUB_TOKEN")
   :repository "guacamoledragon/throw-voice"})

(deftest pr-test
  (let [token (:token config)
        [repo-name repo-owner] (str/split (:repository config) #"/")
        graphql (slurp "resources/github.gql")
        variables {:repoName       repo-name
                   :repoOwner      repo-owner
                   :next           1
                   :orderDirection "ASC"}]
    (is (= 200
           (:status @(gh-graphql/query token graphql variables))))))
