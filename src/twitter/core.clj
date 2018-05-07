(ns twitter.core
  (:require [org.httpkit.client :as http]
            [twitter.oauth :as oauth]))

(def max-tweet-length 280)

(defn oauth-request [config url method params]
  (let [unsigned-oauth (oauth/create-oauth-parameters config)
        signature (oauth/calculate-signature config
                                             (->> unsigned-oauth
                                                  (merge params)
                                                  oauth/generate-parameter-string
                                                  (oauth/generate-base-string url method)))
        signed-oauth (assoc unsigned-oauth :oauth_signature signature)]
    (http/request {:url url
                   :method method
                   :query-params (merge signed-oauth params)})))

(defn update-status [config status]
  (let [url "https://api.twitter.com/1.1/statuses/update.json"
        method :post
        params {:status status}]
    (oauth-request config url method params)))
