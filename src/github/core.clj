(ns github.core
  (:require [clojure.string :as str]
            [github.graphql :as gh-graphql :refer [pull-requests-query]]
            [cheshire.core :as cheshire]))

(def page-info-path [:data :repository :pullRequests :pageInfo])
(def nodes-path [:data :repository :pullRequests :nodes])

(defn config->variables
  [config]
  (let [{:keys [repository]} config
        [repo-owner repo-name] (str/split repository #"/")]
    {:repoName  repo-name
     :repoOwner repo-owner}))

(defn get-pull-requests
  ([token variables] (let [variables (assoc variables :next 5
                                                      :orderDirection "ASC"
                                                      :cursor nil)]
                       (get-pull-requests token variables true [])))
  ([token variables has-next pull-requests] (if (not has-next)
                                              pull-requests
                                              (let [resp @(gh-graphql/query token pull-requests-query variables)
                                                    result (cheshire/parse-string (:body resp) true)
                                                    page-info (get-in result page-info-path)
                                                    prs (get-in result nodes-path)]
                                                (get-pull-requests token
                                                                   (assoc variables :cursor (:endCursor page-info))
                                                                   (:hasNextPage page-info)
                                                                   (into pull-requests prs))))))

(defn get-latest-pull-requests
  ([token variables id] (let [variables (assoc variables :next 5
                                                         :orderDirection "DESC"
                                                         :cursor nil)]
                          (get-latest-pull-requests token variables id true [])))
  ([token variables id has-next pull-requests] (if (not has-next)
                                                 pull-requests
                                                 (let [resp @(gh-graphql/query token pull-requests-query variables)
                                                       result (cheshire/parse-string (:body resp) true)
                                                       page-info (get-in result page-info-path)
                                                       prs (get-in result nodes-path)
                                                       prs-until-id (take-while #(not= id (:id %)) prs)]
                                                   (get-latest-pull-requests token
                                                                             (assoc variables :cursor (:endCursor page-info))
                                                                             id
                                                                             (and (= (count prs) (count prs-until-id))
                                                                                  (:hasNextPage page-info))
                                                                             (into pull-requests prs-until-id))))))
