(function() {
  var $, PivotData, addCommas, aggregatorTemplates, aggregators, buildPivotData, buildPivotTable, convertToArray, dayNames, deriveAttributes, derivers, forEachRow, mthNames, numberFormat, renderers, spanSize, zeroPad;
  var __indexOf = Array.prototype.indexOf || function(item) {
    for (var i = 0, l = this.length; i < l; i++) {
      if (this[i] === item) return i;
    }
    return -1;
  }, __hasProp = Object.prototype.hasOwnProperty, __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };
  $ = jQuery;
  /*
  Utilities
  */
  addCommas = function(nStr) {
    var rgx, x, x1, x2;
    nStr += '';
    x = nStr.split('.');
    x1 = x[0];
    x2 = x.length > 1 ? '.' + x[1] : '';
    rgx = /(\d+)(\d{3})/;
    while (rgx.test(x1)) {
      x1 = x1.replace(rgx, '$1' + ',' + '$2');
    }
    return x1 + x2;
  };
  numberFormat = function(sigfig, scaler) {
    if (sigfig == null) {
      sigfig = 3;
    }
    if (scaler == null) {
      scaler = 1;
    }
    return function(x) {
      if (x === 0 || isNaN(x) || !isFinite(x)) {
        return "";
      } else {
        return addCommas((scaler * x).toFixed(sigfig));
      }
    };
  };
  aggregatorTemplates = {
    sum: function(sigfig, scaler) {
      if (sigfig == null) {
        sigfig = 3;
      }
      if (scaler == null) {
        scaler = 1;
      }
      return function(_arg) {
        var field;
        field = _arg[0];
        return function() {
          return {
            sum: 0,
            push: function(row) {
              if (!isNaN(parseFloat(row[field]))) {
                return this.sum += parseFloat(row[field]);
              }
            },
            value: function() {
              return this.sum;
            },
            format: numberFormat(sigfig, scaler)
          };
        };
      };
    },
    average: function(sigfig, scaler) {
      if (sigfig == null) {
        sigfig = 3;
      }
      if (scaler == null) {
        scaler = 1;
      }
      return function(_arg) {
        var field;
        field = _arg[0];
        return function() {
          return {
            sum: 0,
            len: 0,
            push: function(row) {
              if (!isNaN(parseFloat(row[field]))) {
                this.sum += parseFloat(row[field]);
                return this.len++;
              }
            },
            value: function() {
              return this.sum / this.len;
            },
            format: numberFormat(sigfig, scaler)
          };
        };
      };
    },
    sumOverSum: function(sigfig, scaler) {
      if (sigfig == null) {
        sigfig = 3;
      }
      if (scaler == null) {
        scaler = 1;
      }
      return function(_arg) {
        var denom, num;
        num = _arg[0], denom = _arg[1];
        return function() {
          return {
            sumNum: 0,
            sumDenom: 0,
            push: function(row) {
              if (!isNaN(parseFloat(row[num]))) {
                this.sumNum += parseFloat(row[num]);
              }
              if (!isNaN(parseFloat(row[denom]))) {
                return this.sumDenom += parseFloat(row[denom]);
              }
            },
            value: function() {
              return this.sumNum / this.sumDenom;
            },
            format: numberFormat(sigfig, scaler)
          };
        };
      };
    },
    sumOverSumBound80: function(sigfig, scaler, upper) {
      if (sigfig == null) {
        sigfig = 3;
      }
      if (scaler == null) {
        scaler = 1;
      }
      if (upper == null) {
        upper = true;
      }
      return function(_arg) {
        var denom, num;
        num = _arg[0], denom = _arg[1];
        return function() {
          return {
            sumNum: 0,
            sumDenom: 0,
            push: function(row) {
              if (!isNaN(parseFloat(row[num]))) {
                this.sumNum += parseFloat(row[num]);
              }
              if (!isNaN(parseFloat(row[denom]))) {
                return this.sumDenom += parseFloat(row[denom]);
              }
            },
            value: function() {
              var sign;
              sign = upper ? 1 : -1;
              return (0.821187207574908 / this.sumDenom + this.sumNum / this.sumDenom + 1.2815515655446004 * sign * Math.sqrt(0.410593603787454 / (this.sumDenom * this.sumDenom) + (this.sumNum * (1 - this.sumNum / this.sumDenom)) / (this.sumDenom * this.sumDenom))) / (1 + 1.642374415149816 / this.sumDenom);
            },
            format: numberFormat(sigfig, scaler)
          };
        };
      };
    }
  };
  aggregators = {
    count: function() {
      return function() {
        return {
          count: 0,
          push: function() {
            return this.count++;
          },
          value: function() {
            return this.count;
          },
          format: numberFormat(0)
        };
      };
    },
    countUnique: function(_arg) {
      var field;
      field = _arg[0];
      return function() {
        return {
          uniq: [],
          push: function(row) {
            var _ref;
            if (_ref = row[field], __indexOf.call(this.uniq, _ref) < 0) {
              return this.uniq.push(row[field]);
            }
          },
          value: function() {
            return this.uniq.length;
          },
          format: numberFormat(0)
        };
      };
    },
    listUnique: function(_arg) {
      var field;
      field = _arg[0];
      return function() {
        return {
          uniq: [],
          push: function(row) {
            var _ref;
            if (_ref = row[field], __indexOf.call(this.uniq, _ref) < 0) {
              return this.uniq.push(row[field]);
            }
          },
          value: function() {
            return this.uniq.join(", ");
          },
          format: function(x) {
            return x;
          }
        };
      };
    },
    intSum: aggregatorTemplates.sum(0),
    sum: aggregatorTemplates.sum(3),
    average: aggregatorTemplates.average(3),
    sumOverSum: aggregatorTemplates.sumOverSum(3),
    ub80: aggregatorTemplates.sumOverSumBound80(3, 1, true),
    lb80: aggregatorTemplates.sumOverSumBound80(3, 1, false)
  };
  renderers = {
    "Table": buildPivotData,
    "Table Barchart": function(pvtData) {
      return buildPivotTable(pvtData).barchart();
    },
    "Heatmap": function(pvtData) {
      return buildPivotTable(pvtData).heatmap();
    },
    "Row Heatmap": function(pvtData) {
      return buildPivotTable(pvtData).heatmap("rowheatmap");
    },
    "Col Heatmap": function(pvtData) {
      return buildPivotTable(pvtData).heatmap("colheatmap");
    }
  };
  mthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  zeroPad = function(number) {
    return ("0" + number).substr(-2, 2);
  };
  derivers = {
    bin: function(col, binWidth) {
      return function(row) {
        return row[col] - row[col] % binWidth;
      };
    },
    dateFormat: function(col, formatString) {
      return function(row) {
        var date, dispatch;
        date = new Date(Date.parse(row[col]));
        dispatch = {
          y: function() {
            return date.getFullYear();
          },
          m: function() {
            return zeroPad(date.getMonth() + 1);
          },
          n: function() {
            return mthNames[date.getMonth()];
          },
          d: function() {
            return zeroPad(date.getDate());
          },
          w: function() {
            return dayNames[date.getDay()];
          },
          x: function() {
            return date.getDay();
          },
          H: function() {
            return zeroPad(date.getHours());
          },
          M: function() {
            return zeroPad(date.getMinutes());
          },
          S: function() {
            return zeroPad(date.getSeconds());
          }
        };
        return formatString.replace(/%(.)/g, function(m, p) {
          return dispatch[p]();
        });
      };
    }
  };
  $.pivotUtilities = {
    aggregatorTemplates: aggregatorTemplates,
    aggregators: aggregators,
    renderers: renderers,
    derivers: derivers
  };
  /*
  functions for accessing input
  */
  deriveAttributes = function(row, derivedAttributes, f) {
    var k, v, _ref, _ref2;
    for (k in derivedAttributes) {
      v = derivedAttributes[k];
      row[k] = (_ref = v(row)) != null ? _ref : row[k];
    }
    for (k in row) {
      if (!__hasProp.call(row, k)) continue;
      if ((_ref2 = row[k]) == null) {
        row[k] = "null";
      }
    }
    return f(row);
  };
  forEachRow = function(input, derivedAttributes, f) {
    var addRow, compactRow, i, j, k, row, tblCols, _i, _len, _ref, _results, _results2;
    addRow = function(row) {
      return deriveAttributes(row, derivedAttributes, f);
    };
    if (Object.prototype.toString.call(input) === '[object Function]') {
      return input(addRow);
    } else if (Array.isArray(input)) {
      if (Array.isArray(input[0])) {
        _results = [];
        for (i in input) {
          if (!__hasProp.call(input, i)) continue;
          compactRow = input[i];
          if (i > 0) {
            row = {};
            _ref = input[0];
            for (j in _ref) {
              if (!__hasProp.call(_ref, j)) continue;
              k = _ref[j];
              row[k] = compactRow[j];
            }
            _results.push(addRow(row));
          }
        }
        return _results;
      } else {
        _results2 = [];
        for (_i = 0, _len = input.length; _i < _len; _i++) {
          row = input[_i];
          _results2.push(addRow(row));
        }
        return _results2;
      }
    } else {
      tblCols = [];
      $("thead > tr > th", input).each(function(i) {
        return tblCols.push($(this).text());
      });
      return $("tbody > tr", input).each(function(i) {
        row = {};
        $("td", this).each(function(j) {
          return row[tblCols[j]] = $(this).text();
        });
        return addRow(row);
      });
    }
  };
  convertToArray = function(input) {
    var result;
    result = [];
    forEachRow(input, {}, function(row) {
      return result.push(row);
    });
    return result;
  };
  PivotData = (function() {
    function PivotData(aggregator, colVars, rowVars) {
      this.aggregator = aggregator;
      this.colVars = colVars;
      this.rowVars = rowVars;
      this.getAggregator = __bind(this.getAggregator, this);
      this.flattenKey = __bind(this.flattenKey, this);
      this.getRowKeys = __bind(this.getRowKeys, this);
      this.getColKeys = __bind(this.getColKeys, this);
      this.sortKeys = __bind(this.sortKeys, this);
      this.arrSort = __bind(this.arrSort, this);
      this.natSort = __bind(this.natSort, this);
      this.tree = {};
      this.rowKeys = [];
      this.colKeys = [];
      this.flatRowKeys = [];
      this.flatColKeys = [];
      this.rowTotals = {};
      this.colTotals = {};
      this.allTotal = this.aggregator();
      this.sorted = false;
    }
    PivotData.prototype.natSort = function(as, bs) {
      var a, a1, b, b1, rd, rx, rz;
      rx = /(\d+)|(\D+)/g;
      rd = /\d/;
      rz = /^0/;
      if (typeof as === "number" || typeof bs === "number") {
        if (isNaN(as)) {
          return 1;
        }
        if (isNaN(bs)) {
          return -1;
        }
        return as - bs;
      }
      a = String(as).toLowerCase();
      b = String(bs).toLowerCase();
      if (a === b) {
        return 0;
      }
      if (!(rd.test(a) && rd.test(b))) {
        if (a > b) {
          return 1;
        } else {
          return -1;
        }
      }
      a = a.match(rx);
      b = b.match(rx);
      while (a.length && b.length) {
        a1 = a.shift();
        b1 = b.shift();
        if (a1 !== b1) {
          if (rd.test(a1) && rd.test(b1)) {
            return a1.replace(rz, ".0") - b1.replace(rz, ".0");
          } else {
            if (a1 > b1) {
              return 1;
            } else {
              return -1;
            }
          }
        }
      }
      return a.length - b.length;
    };
    PivotData.prototype.arrSort = function(a, b) {
      return this.natSort(a.join(), b.join());
    };
    PivotData.prototype.sortKeys = function() {
      if (!this.sorted) {
        this.rowKeys.sort(this.arrSort);
        this.colKeys.sort(this.arrSort);
      }
      return this.sorted = true;
    };
    PivotData.prototype.getColKeys = function() {
      this.sortKeys();
      return this.colKeys;
    };
    PivotData.prototype.getRowKeys = function() {
      this.sortKeys();
      return this.rowKeys;
    };
    PivotData.prototype.flattenKey = function(x) {
      return x.join(String.fromCharCode(0));
    };
    PivotData.prototype.processRow = function(row) {
      var colKey, flatColKey, flatRowKey, rowKey, x;
      colKey = (function() {
        var _i, _len, _ref, _results;
        _ref = this.colVars;
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          x = _ref[_i];
          _results.push(row[x]);
        }
        return _results;
      }).call(this);
      rowKey = (function() {
        var _i, _len, _ref, _results;
        _ref = this.rowVars;
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          x = _ref[_i];
          _results.push(row[x]);
        }
        return _results;
      }).call(this);
      flatRowKey = this.flattenKey(rowKey);
      flatColKey = this.flattenKey(colKey);
      this.allTotal.push(row);
      if (rowKey.length !== 0) {
        if (__indexOf.call(this.flatRowKeys, flatRowKey) < 0) {
          this.rowKeys.push(rowKey);
          this.flatRowKeys.push(flatRowKey);
        }
        if (!this.rowTotals[flatRowKey]) {
          this.rowTotals[flatRowKey] = this.aggregator();
        }
        this.rowTotals[flatRowKey].push(row);
      }
      if (colKey.length !== 0) {
        if (__indexOf.call(this.flatColKeys, flatColKey) < 0) {
          this.colKeys.push(colKey);
          this.flatColKeys.push(flatColKey);
        }
        if (!this.colTotals[flatColKey]) {
          this.colTotals[flatColKey] = this.aggregator();
        }
        this.colTotals[flatColKey].push(row);
      }
      if (colKey.length !== 0 && rowKey.length !== 0) {
        if (!(flatRowKey in this.tree)) {
          this.tree[flatRowKey] = {};
        }
        if (!(flatColKey in this.tree[flatRowKey])) {
          this.tree[flatRowKey][flatColKey] = this.aggregator();
        }
        return this.tree[flatRowKey][flatColKey].push(row);
      }
    };
    PivotData.prototype.getAggregator = function(rowKey, colKey) {
      var agg, flatColKey, flatRowKey;
      flatRowKey = this.flattenKey(rowKey);
      flatColKey = this.flattenKey(colKey);
      if (rowKey.length === 0 && colKey.length === 0) {
        agg = this.allTotal;
      } else if (rowKey.length === 0) {
        agg = this.colTotals[flatColKey];
      } else if (colKey.length === 0) {
        agg = this.rowTotals[flatRowKey];
      } else {
        agg = this.tree[flatRowKey][flatColKey];
      }
      return agg != null ? agg : {
        value: (function() {
          return null;
        }),
        format: function() {
          return "";
        }
      };
    };
    return PivotData;
  })();
  buildPivotData = function(input, cols, rows, aggregator, filter, derivedAttributes) {
    var pivotData;
    pivotData = new PivotData(aggregator, cols, rows);
    forEachRow(input, derivedAttributes, function(row) {
      if (filter(row)) {
        return pivotData.processRow(row);
      }
    });
    return pivotData;
  };
  spanSize = function(arr, i, j) {
    var len, noDraw, stop, x;
    if (i !== 0) {
      noDraw = true;
      for (x = 0; 0 <= j ? x <= j : x >= j; 0 <= j ? x++ : x--) {
        if (arr[i - 1][x] !== arr[i][x]) {
          noDraw = false;
        }
      }
      if (noDraw) {
        return -1;
      }
    }
    len = 0;
    while (i + len < arr.length) {
      stop = false;
      for (x = 0; 0 <= j ? x <= j : x >= j; 0 <= j ? x++ : x--) {
        if (arr[i][x] !== arr[i + len][x]) {
          stop = true;
        }
      }
      if (stop) {
        break;
      }
      len++;
    }
    return len;
  };
  buildPivotTable = function(pivotData) {
    var aggregator, c, colKey, colKeys, cols, i, j, r, result, rowKey, rowKeys, rows, th, totalAggregator, tr, txt, val, x;
    cols = pivotData.colVars;
    rows = pivotData.rowVars;
    rowKeys = pivotData.getRowKeys();
    colKeys = pivotData.getColKeys();
    result = $("<table class='table table-bordered pvtTable'>");
    for (j in cols) {
      if (!__hasProp.call(cols, j)) continue;
      c = cols[j];
      tr = $("<tr>");
      if (parseInt(j) === 0 && rows.length !== 0) {
        tr.append($("<th>").attr("colspan", rows.length).attr("rowspan", cols.length));
      }
      tr.append($("<th class='pvtAxisLabel'>").text(c));
      for (i in colKeys) {
        if (!__hasProp.call(colKeys, i)) continue;
        colKey = colKeys[i];
        x = spanSize(colKeys, parseInt(i), parseInt(j));
        if (x !== -1) {
          th = $("<th class='pvtColLabel'>").text(colKey[j]).attr("colspan", x);
          if (parseInt(j) === cols.length - 1 && rows.length !== 0) {
            th.attr("rowspan", 2);
          }
          tr.append(th);
        }
      }
      if (parseInt(j) === 0) {
        tr.append($("<th class='pvtTotalLabel'>").text("Totals").attr("rowspan", cols.length + (rows.length === 0 ? 0 : 1)));
      }
      result.append(tr);
    }
    if (rows.length !== 0) {
      tr = $("<tr>");
      for (i in rows) {
        if (!__hasProp.call(rows, i)) continue;
        r = rows[i];
        tr.append($("<th class='pvtAxisLabel'>").text(r));
      }
      th = $("<th>");
      if (cols.length === 0) {
        th.addClass("pvtTotalLabel").text("Totals");
      }
      tr.append(th);
      result.append(tr);
    }
    for (i in rowKeys) {
      if (!__hasProp.call(rowKeys, i)) continue;
      rowKey = rowKeys[i];
      tr = $("<tr>");
      for (j in rowKey) {
        if (!__hasProp.call(rowKey, j)) continue;
        txt = rowKey[j];
        x = spanSize(rowKeys, parseInt(i), parseInt(j));
        if (x !== -1) {
          th = $("<th class='pvtRowLabel'>").text(txt).attr("rowspan", x);
          if (parseInt(j) === rows.length - 1 && cols.length !== 0) {
            th.attr("colspan", 2);
          }
          tr.append(th);
        }
      }
      for (j in colKeys) {
        if (!__hasProp.call(colKeys, j)) continue;
        colKey = colKeys[j];
        aggregator = pivotData.getAggregator(rowKey, colKey);
        val = aggregator.value();
        tr.append($("<td class='pvtVal row" + i + " col" + j + "'>").text(aggregator.format(val)).data("value", val));
      }
      totalAggregator = pivotData.getAggregator(rowKey, []);
      val = totalAggregator.value();
      tr.append($("<td class='pvtTotal rowTotal'>").text(totalAggregator.format(val)).data("value", val).data("for", "row" + i));
      result.append(tr);
    }
    tr = $("<tr>");
    th = $("<th class='pvtTotalLabel'>").text("Totals");
    th.attr("colspan", rows.length + (cols.length === 0 ? 0 : 1));
    tr.append(th);
    for (j in colKeys) {
      if (!__hasProp.call(colKeys, j)) continue;
      colKey = colKeys[j];
      totalAggregator = pivotData.getAggregator([], colKey);
      val = totalAggregator.value();
      tr.append($("<td class='pvtTotal colTotal'>").text(totalAggregator.format(val)).data("value", val).data("for", "col" + j));
    }
    totalAggregator = pivotData.getAggregator([], []);
    val = totalAggregator.value();
    tr.append($("<td class='pvtGrandTotal'>").text(totalAggregator.format(val)).data("value", val));
    result.append(tr);
    result.data("dimensions", [rowKeys.length, colKeys.length]);
    return result;
  };
  /*
  Pivot Table
  */
  $.fn.pivot = function(input, opts) {
    var defaults, pivotData;
    defaults = {
      cols: [],
      rows: [],
      filter: function() {
        return true;
      },
      aggregator: aggregators.count(),
      derivedAttributes: {},
      renderer: function(pivotData) {
        return buildPivotTable(pivotData);
      }
    };
    opts = $.extend(defaults, opts);
    pivotData = buildPivotData(input, opts.cols, opts.rows, opts.aggregator, opts.filter, opts.derivedAttributes);
    this.html(opts.renderer(pivotData));
    return this;
  };
  /*
  UI code, calls pivot table above
  */
  $.fn.pivotUI = function(input, opts) {
    var aggregator, axisValues, c, colList, controls, defaults, first, form, k, pivotTable, radio, refresh, rendererNames, tblCols, tr1, tr2, uiTable, x, y, _i, _j, _k, _l, _len, _len2, _len3, _len4, _len5, _len6, _m, _n, _ref, _ref2, _ref3, _ref4, _ref5;
    defaults = {
      derivedAttributes: {},
      aggregators: aggregators,
      renderers: renderers,
      hiddenAxes: [],
      cols: [],
      rows: [],
      vals: []
    };
    opts = $.extend(defaults, opts);
    input = convertToArray(input);
    tblCols = (function() {
      var _ref, _results;
      _ref = input[0];
      _results = [];
      for (k in _ref) {
        if (!__hasProp.call(_ref, k)) continue;
        _results.push(k);
      }
      return _results;
    })();
    _ref = opts.derivedAttributes;
    for (c in _ref) {
      if (!__hasProp.call(_ref, c)) continue;
      if ((__indexOf.call(tblCols, c) < 0)) {
        tblCols.push(c);
      }
    }
    axisValues = {};
    for (_i = 0, _len = tblCols.length; _i < _len; _i++) {
      x = tblCols[_i];
      axisValues[x] = {};
    }
    forEachRow(input, opts.derivedAttributes, function(row) {
      var k, v, _base, _ref2, _results;
      _results = [];
      for (k in row) {
        if (!__hasProp.call(row, k)) continue;
        v = row[k];
        if (v == null) {
          v = "null";
        }
        if ((_ref2 = (_base = axisValues[k])[v]) == null) {
          _base[v] = 0;
        }
        _results.push(axisValues[k][v]++);
      }
      return _results;
    });
    uiTable = $("<table class='table table-bordered' cellpadding='5'>");
    rendererNames = (function() {
      var _ref2, _results;
      _ref2 = opts.renderers;
      _results = [];
      for (x in _ref2) {
        if (!__hasProp.call(_ref2, x)) continue;
        y = _ref2[x];
        _results.push(x);
      }
      return _results;
    })();
    if (rendererNames.length !== 0) {
      controls = $("<td colspan='2' align='center'>");
      form = $("<form>").addClass("form-inline");
      controls.append(form);
      first = true;
      for (_j = 0, _len2 = rendererNames.length; _j < _len2; _j++) {
        x = rendererNames[_j];
        radio = $("<input type='radio' name='renderers' id='renderers_" + (x.replace(/\s/g, "")) + "'>").css({
          "margin-left": "15px",
          "margin-right": "5px"
        }).val(x);
        if (first) {
          radio.attr("checked", "checked");
          first = false;
        }
        form.append(radio).append($("<label class='checkbox inline' for='renderers_" + (x.replace(/\s/g, "")) + "'>").text(x));
      }
      uiTable.append($("<tr>").append(controls));
    }
    colList = $("<td colspan='2' id='unused' class='pvtAxisContainer pvtHorizList'>");
    for (_k = 0, _len3 = tblCols.length; _k < _len3; _k++) {
      c = tblCols[_k];
      if (__indexOf.call(opts.hiddenAxes, c) < 0) {
        (function(c) {
          var btns, colLabel, filterItem, k, numKeys, v, valueList, _l, _len4, _ref2;
          numKeys = Object.keys(axisValues[c]).length;
          colLabel = $("<nobr>").text(c);
          valueList = $("<div>").css({
            "z-index": 100,
            "width": "280px",
            "height": "350px",
            "overflow": "scroll",
            "border": "1px solid gray",
            "background": "white",
            "display": "none",
            "position": "absolute",
            "padding": "20px"
          });
          valueList.append($("<strong>").text("" + numKeys + " values for " + c));
          if (numKeys > 50) {
            valueList.append($("<p>").text("(too many to list)"));
          } else {
            btns = $("<p>");
            btns.append($("<button>").text("Select All").bind("click", function() {
              return valueList.find("input").attr("checked", true);
            }));
            btns.append($("<button>").text("Select None").bind("click", function() {
              return valueList.find("input").attr("checked", false);
            }));
            valueList.append(btns);
            _ref2 = Object.keys(axisValues[c]).sort();
            for (_l = 0, _len4 = _ref2.length; _l < _len4; _l++) {
              k = _ref2[_l];
              v = axisValues[c][k];
              filterItem = $("<label>");
              filterItem.append($("<input type='checkbox' class='pvtFilter'>").attr("checked", true).data("filter", [c, k]));
              filterItem.append($("<span>").text("" + k + " (" + v + ")"));
              valueList.append($("<p>").append(filterItem));
            }
          }
          colLabel.bind("dblclick", function(e) {
            valueList.css({
              left: e.pageX,
              top: e.pageY
            }).toggle();
            valueList.bind("click", function(e) {
              return e.stopPropagation();
            });
            return $(document).one("click", function() {
              refresh();
              return valueList.toggle();
            });
          });
          return colList.append($("<li class='label label-info' id='axis_" + (c.replace(/\s/g, "")) + "'>").append(colLabel).append(valueList));
        })(c);
      }
    }
    uiTable.append($("<tr>").append(colList));
    tr1 = $("<tr>");
    aggregator = $("<select id='aggregator'>").css("margin-bottom", "5px").bind("change", function() {
      return refresh();
    });
    _ref2 = opts.aggregators;
    for (x in _ref2) {
      if (!__hasProp.call(_ref2, x)) continue;
      aggregator.append($("<option>").val(x).text(x));
    }
    tr1.append($("<td id='vals' class='pvtAxisContainer pvtHorizList'>").css("text-align", "center").append(aggregator).append($("<br>")));
    tr1.append($("<td id='cols' class='pvtAxisContainer pvtHorizList'>"));
    uiTable.append(tr1);
    tr2 = $("<tr>");
    tr2.append($("<td valign='top' id='rows' class='pvtAxisContainer'>"));
    pivotTable = $("<td valign='top'>");
    tr2.append(pivotTable);
    uiTable.append(tr2);
    this.html(uiTable);
    _ref3 = opts.cols;
    for (_l = 0, _len4 = _ref3.length; _l < _len4; _l++) {
      x = _ref3[_l];
      $("#cols").append($("#axis_" + (x.replace(/\s/g, ""))));
    }
    _ref4 = opts.rows;
    for (_m = 0, _len5 = _ref4.length; _m < _len5; _m++) {
      x = _ref4[_m];
      $("#rows").append($("#axis_" + (x.replace(/\s/g, ""))));
    }
    _ref5 = opts.vals;
    for (_n = 0, _len6 = _ref5.length; _n < _len6; _n++) {
      x = _ref5[_n];
      $("#vals").append($("#axis_" + (x.replace(/\s/g, ""))));
    }
    if (opts.aggregatorName != null) {
      $("#aggregator").val(opts.aggregatorName);
    }
    if (opts.rendererName != null) {
      $("#renderers_" + (opts.rendererName.replace(/\s/g, ""))).attr('checked', true);
    }
    refresh = function() {
      var exclusions, renderer, subopts, vals;
      subopts = {
        derivedAttributes: opts.derivedAttributes
      };
      subopts.cols = [];
      subopts.rows = [];
      vals = [];
      $("#rows li nobr").each(function() {
        return subopts.rows.push($(this).text());
      });
      $("#cols li nobr").each(function() {
        return subopts.cols.push($(this).text());
      });
      $("#vals li nobr").each(function() {
        return vals.push($(this).text());
      });
      subopts.aggregator = opts.aggregators[aggregator.val()](vals);
      exclusions = [];
      $('input.pvtFilter').not(':checked').each(function() {
        return exclusions.push($(this).data("filter"));
      });
      subopts.filter = function(row) {
        var v, _len7, _o, _ref6;
        for (_o = 0, _len7 = exclusions.length; _o < _len7; _o++) {
          _ref6 = exclusions[_o], k = _ref6[0], v = _ref6[1];
          if (row[k] === v) {
            return false;
          }
        }
        return true;
      };
      if (rendererNames.length !== 0) {
        renderer = $('input[name=renderers]:checked').val();
        subopts.renderer = opts.renderers[renderer];
      }
      return pivotTable.pivot(input, subopts);
    };
    refresh();
    $('input[name=renderers]').bind("change", refresh);
    $(".pvtAxisContainer").sortable({
      connectWith: ".pvtAxisContainer",
      items: 'li'
    }).bind("sortstop", refresh);
    return this;
  };
  /*
  Heatmap post-processing
  */
  $.fn.heatmap = function(scope) {
    var colorGen, heatmapper, i, j, numCols, numRows, _ref;
    if (scope == null) {
      scope = "heatmap";
    }
    _ref = this.data("dimensions"), numRows = _ref[0], numCols = _ref[1];
    colorGen = function(color, min, max) {
      var hexGen;
      hexGen = (function() {
        switch (color) {
          case "red":
            return function(hex) {
              return "ff" + hex + hex;
            };
          case "green":
            return function(hex) {
              return "" + hex + "ff" + hex;
            };
          case "blue":
            return function(hex) {
              return "" + hex + hex + "ff";
            };
        }
      })();
      return function(x) {
        var hex, intensity;
        intensity = 255 - Math.round(255 * (x - min) / (max - min));
        hex = intensity.toString(16).split(".")[0];
        if (hex.length === 1) {
          hex = 0 + hex;
        }
        return hexGen(hex);
      };
    };
    heatmapper = __bind(function(scope, color) {
      var colorFor, forEachCell, values;
      forEachCell = __bind(function(f) {
        return this.find(scope).each(function() {
          var x;
          x = $(this).data("value");
          if ((x != null) && isFinite(x)) {
            return f(x, $(this));
          }
        });
      }, this);
      values = [];
      forEachCell(function(x) {
        return values.push(x);
      });
      colorFor = colorGen(color, Math.min.apply(Math, values), Math.max.apply(Math, values));
      return forEachCell(function(x, elem) {
        return elem.css("background-color", "#" + colorFor(x));
      });
    }, this);
    switch (scope) {
      case "heatmap":
        heatmapper(".pvtVal", "red");
        break;
      case "rowheatmap":
        for (i = 0; 0 <= numRows ? i < numRows : i > numRows; 0 <= numRows ? i++ : i--) {
          heatmapper(".pvtVal.row" + i, "red");
        }
        break;
      case "colheatmap":
        for (j = 0; 0 <= numCols ? j < numCols : j > numCols; 0 <= numCols ? j++ : j--) {
          heatmapper(".pvtVal.col" + j, "red");
        }
    }
    heatmapper(".pvtTotal.rowTotal", "red");
    heatmapper(".pvtTotal.colTotal", "red");
    return this;
  };
  /*
  Barchart post-processing
  */
  $.fn.barchart = function() {
    var barcharter, i, numCols, numRows, _ref;
    _ref = this.data("dimensions"), numRows = _ref[0], numCols = _ref[1];
    barcharter = __bind(function(scope) {
      var forEachCell, max, scaler, values;
      forEachCell = __bind(function(f) {
        return this.find(scope).each(function() {
          var x;
          x = $(this).data("value");
          if ((x != null) && isFinite(x)) {
            return f(x, $(this));
          }
        });
      }, this);
      values = [];
      forEachCell(function(x) {
        return values.push(x);
      });
      max = Math.max.apply(Math, values);
      scaler = function(x) {
        return 100 * x / (1.4 * max);
      };
      return forEachCell(function(x, elem) {
        var text, wrapper;
        text = elem.text();
        wrapper = $("<div>").css({
          "position": "relative",
          "height": "55px"
        });
        wrapper.append($("<div>").css({
          "position": "absolute",
          "bottom": 0,
          "left": 0,
          "right": 0,
          "height": scaler(x) + "%",
          "background-color": "gray"
        }));
        wrapper.append($("<div>").text(text).css({
          "position": "relative",
          "padding-left": "5px",
          "padding-right": "5px"
        }));
        return elem.css({
          "padding": 0,
          "padding-top": "5px",
          "text-align": "center"
        }).html(wrapper);
      });
    }, this);
    for (i = 0; 0 <= numRows ? i < numRows : i > numRows; 0 <= numRows ? i++ : i--) {
      barcharter(".pvtVal.row" + i);
    }
    barcharter(".pvtTotal.colTotal");
    return this;
  };
}).call(this);
