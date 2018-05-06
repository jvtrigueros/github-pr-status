(ns github.graphql
  (:require [org.httpkit.client :as http]
            [cheshire.core :as cheshire]))

(def pull-requests-query (slurp "resources/github.gql"))

(defn query
  [token query variables]
  (let [body (cheshire/generate-string {:query query :variables variables})
        url "https://api.github.com/graphql"]
    (http/request
      {:url         url
       :oauth-token token
       :method      :post
       :body        body})))
