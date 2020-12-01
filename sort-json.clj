#!/usr/local/bin/bb
;; sort-json.clj
;; Sort a json file (list of maps) by a key.
;; Default sort key if not specified is "id".
;; Usage
;; ./sort-json.clj MY-FILE.json
;; ./sort-json.clj MY-FILE.json mysortkey

(require '[cheshire.core :as json]
         '[clojure.string :as string])

(defn prep-json
  "Load json and set keywords"
  [file]
  (-> file
      (slurp)
      (json/parse-string true)))

(defn sort-json
  "Sort a json file by key."
  [file sort-key]
  (let [json-file (prep-json file)]
    (sort-by (keyword sort-key) json-file)))

(defn output-name
  "Create the output filename."
  [file]
  (let [part1 (string/replace file ".json" "")
        parts [part1 "sorted"]]
    (str (string/join "_" parts) ".json")))

(defn show-help
  "Display a help message."
  []
  (println "Usage")
  (println "./sort-json.clj MY-FILE.json")
  (println "./sort-json.clj MY-FILE.json mysortkey"))

(defn -main
  ([file]
    (-main file :id))
  ([file sort-key]
    (let [sorted-json (sort-json file sort-key)
          output (output-name file)]
      (spit output (json/generate-string sorted-json {:pretty true})))))

(condp #(= %1 (count %2)) *command-line-args*
  0 (show-help)
  1 (-main (first *command-line-args*))
  2 (-main (first *command-line-args*) (second *command-line-args*))
  (show-help))
