(ns bus-routes.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bus-routes.core-test]))

(doo-tests 'bus-routes.core-test)

