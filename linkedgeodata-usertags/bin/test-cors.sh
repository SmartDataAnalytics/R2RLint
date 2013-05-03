#!/bin/bash
# Source: http://stackoverflow.com/questions/12173990/how-can-you-debug-cors-request-with-curl#
curl -H "Origin: http://example.com" --verbose localhost:5522/sparql-analytics/api/sparql
