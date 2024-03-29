#!/usr/bin/env bb
;; sort-json.clj
;; Sort a json file (list of maps) by a key.
;; Default sort key if not specified is "id".

(require '[babashka.cli :as cli]
         '[babashka.fs :as fs]
         '[cheshire.core :as json]
         '[clojure.string :as string])

;;; core functions

(defn prep-json
  "Load json and set keywords."
  [file]
  (-> file
      (slurp)
      (json/parse-string true)))

(defn sort-json
  "Sort a json `file` by `sort-key`."
  [file sort-key]
  (let [json-file (prep-json file)]
    (sort-by (keyword sort-key) json-file)))

(defn output-name
  "Create the output filename."
  [file]
  (let [part1 (string/replace file ".json" "")
        parts [part1 "sorted"]]
    (str (string/join "_" parts) ".json")))

(defn process-json
  "Sort and write the new json file."
  [{:keys [file key]}]
  (let [sorted-json (sort-json file key)
        outfile (output-name file)]
    (->> (json/generate-string sorted-json {:pretty true})
         (spit outfile))
    (println (format "Wrote %s" outfile))))

;;; cli config

(defn show-help
  [opts]
  (println "sort-json - Sort a JSON file that has a list of maps by an optional key.")
  (println "Usage")
  (println (cli/format-opts opts)))

(defn file-exists?
  [path]
  (fs/exists? path))

(def cli-opts
  {:spec
   {:file {:desc "JSON file to sort."
           :alias :f
           :validate file-exists?
           :require true}
    :key {:desc "JSON key to sort by."
          :alias :k
          :default-desc "id"
          :default :id}
    :help {:desc "Show help."
           :alias :h
           :coerce :boolean}}
   :error-fn
   (fn [{:keys [spec type cause msg option] :as data}]
     (if (= :org.babashka/cli type)
       (case cause
         :require
           (do (println
                (format "Missing required argument: %s\n" option))
               (show-help cli-opts))
         :validate
           (println
            (format "%s does not exist!\n" msg))
         :default
           (println msg))
        (throw (ex-info msg data)))
       (System/exit 1))})

;;; main

(defn -main
  "sort-json entry point."
  [args]
  (let [opts (cli/parse-opts args cli-opts)]
    (if (or (:help opts) (:h opts))
      (println (show-help cli-opts))
      (do (println "Processing JSON with options:" opts)
          (process-json opts)))))

(-main *command-line-args*)
