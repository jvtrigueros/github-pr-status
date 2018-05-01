(ns twitter.oauth-test
  (:require [clojure.test :refer [deftest is]]
            [twitter.oauth :as oauth]))

(deftest test-percent-encode
  (let [url "https://api.twitter.com/1.1/statuses/update.json"]
    (is (= (oauth/percent-encode url)
           "https%3A%2F%2Fapi.twitter.com%2F1.1%2Fstatuses%2Fupdate.json"))))
