var lastButtonPushed;
var app;
var data0; //chart data
var data1;
var data2;
var data3;
var chart0;
var chart1;
var chart2;
var chart3;
var lastDataSet0;
var lastDataSet1;
var lastDataSet2;
var lastDataSet3;
var lastDataSet = [lastDataSet0, lastDataSet1, lastDataSet2, lastDataSet3];
var radioButtonUnit = [1, 1, 1, 1];

var lastMagicNumber = ["", "", "", ""]; //contains the last called magic number per tab

const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
];

const dailyMagicNumbers = ["B20D1AD6", "05C7CFB1", "CA6D6472", "E04C3900", "FCF4E78D", "0DF164DE"];
const monthlyMagicNumbers = ["921997EE", "5D34D09D", "431509D1", "F28341E2", "2F0A6B15", "6B5A56C2"];
const yearlyMagicNumbers = ["19B814F2", "C55EF32E", "E5FBCC6F", "70BD7C46", "4C14CC7C", "34ECA9CA"];
const minuteMagicNumbers = ["50B441C1", "1D49380A", "A7C708EB", "669D02FE", "A60082A9", "9247DB99", "5293B668", "76C9A0BD"];

var mapMessageText = new Map();
mapMessageText.set("B20D1AD6", "Triggered to load data. Daily energy feed into grid.");
mapMessageText.set("05C7CFB1", "Triggered to load data. Daily energy load from grid.");
mapMessageText.set("CA6D6472", "Triggered to load data. Daily usage of haushold energy.");
mapMessageText.set("E04C3900", "Triggered to load data. Daily energy on AC output (Eac).");
mapMessageText.set("FCF4E78D", "Triggered to load data. Daily energy on DC input A (Edc A).");
mapMessageText.set("0DF164DE", "Triggered to load data. Daily energy on DC input B (Edc B).");

mapMessageText.set("921997EE", "Triggered to load data. Monthly energy feed into grid.");
mapMessageText.set("5D34D09D", "Triggered to load data. Monthly energy load from grid.");
mapMessageText.set("431509D1", "Triggered to load data. Monthly usage of haushold energy.");
mapMessageText.set("F28341E2", "Triggered to load data. Monthly energy on AC output (Eac).");
mapMessageText.set("2F0A6B15", "Triggered to load data. Monthly energy on DC input A (Edc A).");
mapMessageText.set("6B5A56C2", "Triggered to load data. Monthly energy on DC input B (Edc B).");

mapMessageText.set("19B814F2", "Triggered to load data. Yearly energy feed into grid.");
mapMessageText.set("C55EF32E", "Triggered to load data. Yearly energy load from grid.");
mapMessageText.set("E5FBCC6F", "Triggered to load data. Yearly usage of haushold energy.");
mapMessageText.set("70BD7C46", "Triggered to load data. Yearly energy on AC output (Eac).");
mapMessageText.set("4C14CC7C", "Triggered to load data. Yearly energy on DC input A (Edc A).");
mapMessageText.set("34ECA9CA", "Triggered to load data. Yearly energy on DC input B (Edc B).");

mapMessageText.set("A60082A9", "Triggered to load data. 5 minute energy feed into grid.");
mapMessageText.set("9247DB99", "Triggered to load data. 5 minute energy load from grid.");
mapMessageText.set("A7C708EB", "Triggered to load data. 5 minute usage of haushold energy.");
mapMessageText.set("669D02FE", "Triggered to load data. 5 minute energy on AC output (Eac).");
mapMessageText.set("50B441C1", "Triggered to load data. 5 minute energy on DC input A (Edc A).");
mapMessageText.set("1D49380A", "Triggered to load data. 5 minute energy on DC input B (Edc B).");
mapMessageText.set("5293B668", "Triggered to load data. 5 minute values for SoC.");
mapMessageText.set("76C9A0BD", "Triggered to load data. 5 minute values for SoC target.");

var mapHeadlineText = new Map();
mapHeadlineText.set("B20D1AD6", "Daily energy feed into grid.");
mapHeadlineText.set("05C7CFB1", "Daily energy load from grid.");
mapHeadlineText.set("CA6D6472", "Daily usage of haushold energy.");
mapHeadlineText.set("E04C3900", "Daily energy on AC output (Eac).");
mapHeadlineText.set("FCF4E78D", "Daily energy on DC input A (Edc A).");
mapHeadlineText.set("0DF164DE", "Daily energy on DC input B (Edc B).");

mapHeadlineText.set("921997EE", "Monthly energy feed into grid.");
mapHeadlineText.set("5D34D09D", "Monthly energy load from grid.");
mapHeadlineText.set("431509D1", "Monthly usage of haushold energy.");
mapHeadlineText.set("F28341E2", "Monthly energy on AC output (Eac).");
mapHeadlineText.set("2F0A6B15", "Monthly energy on DC input A (Edc A).");
mapHeadlineText.set("6B5A56C2", "Monthly energy on DC input B (Edc B).");

mapHeadlineText.set("19B814F2", "Yearly energy feed into grid.");
mapHeadlineText.set("C55EF32E", "Yearly energy load from grid.");
mapHeadlineText.set("E5FBCC6F", "Yearly usage of haushold energy.");
mapHeadlineText.set("70BD7C46", "Yearly energy on AC output (Eac).");
mapHeadlineText.set("4C14CC7C", "Yearly energy on DC input A (Edc A).");
mapHeadlineText.set("34ECA9CA", "Yearly energy on DC input B (Edc B).");

mapHeadlineText.set("A60082A9", "5 minute energy feed into grid.");
mapHeadlineText.set("9247DB99", "5 minute energy load from grid.");
mapHeadlineText.set("A7C708EB", "5 minute usage of haushold energy.");
mapHeadlineText.set("669D02FE", "5 minute energy on AC output (Eac).");
mapHeadlineText.set("50B441C1", "5 minute energy on DC input A (Edc A).");
mapHeadlineText.set("1D49380A", "5 minute energy on DC input B (Edc B).");
mapHeadlineText.set("5293B668", "5 minute values for SoC.");
mapHeadlineText.set("76C9A0BD", "5 minute values for SoC target.");

function getDataSuccess(returnData) {
  var resultJson = JSON.parse(returnData);
  let currentTab = app.tabActive;
  lastDataSet[currentTab] = resultJson.valueArray;
  lastMagicNumber[currentTab] = resultJson.magicNumber;

  switch(currentTab)
  {
    case 0:
      app.chartTitle0 = mapHeadlineText.get(resultJson.magicNumber);
      break;
    case 1:
      app.chartTitle1 = mapHeadlineText.get(resultJson.magicNumber);
      break;
    case 2:
      app.chartTitle2 = mapHeadlineText.get(resultJson.magicNumber);
      break;
    case 3:
      app.chartTitle3 = mapHeadlineText.get(resultJson.magicNumber);
      break;
  }

  refreshChart(radioButtonUnit[currentTab]);
}

function refreshChart(newUnitConvertFactor)
{
  let timezoneOffset = new Date().getTimezoneOffset() * 60000; //offset given in minutes, need ms
  let currentTab = app.tabActive;
  let chartObject = "chart" + currentTab;
  let dataObject = "data" + currentTab;

  this[chartObject].detach();       //need dynamic access to chart object and data depending on active tab not to change any data on the other tabs
  this[dataObject].labels = [];
  this[dataObject].series[0] = [];

  for (var i = 0; i < lastDataSet[currentTab].length; i++) {

    let timeMs = lastDataSet[currentTab][i].timestamp + timezoneOffset; //time comes in UTC, assuming that inverter and client is in same timezone, so just take offset of client
    let time = new Date(timeMs);
    let value = lastDataSet[currentTab][i].value / newUnitConvertFactor;

    let day = time.getDate().toString();
    let month = monthNames[(time.getMonth())];
    let year = time.getFullYear();
    let hours = time.getHours();
    let minutesUncut = "00" + time.getMinutes();
    let minutes = minutesUncut.substr(minutesUncut.length-2);

    if (dailyMagicNumbers.indexOf(lastMagicNumber[currentTab]) > -1) {
      this[dataObject].labels.push(month + '-' + day);
    } else if (monthlyMagicNumbers.indexOf(lastMagicNumber[currentTab]) > -1){
      this[dataObject].labels.push(month);
    }else if (yearlyMagicNumbers.indexOf(lastMagicNumber[currentTab]) > -1){
      this[dataObject].labels.push(year);
    }
    else {  //5 minute values
      this[dataObject].labels.push(hours+":"+minutes);
    }
    this[dataObject].series[0].push(value);
  }

  this[chartObject].update();
  app.showAlertBar("Data updated successfully.", 3, "info");
}

function getDataError(jqXHR, textStatus, errorThrown)
{
  var resultJson = JSON.parse(jqXHR.responseText);
  app.showAlertBar("Response code " + jqXHR.status + ": " + resultJson.text, 7, "danger");
}

function loadData(magicNumber) {
  //this.app.__vue__.showvarT();
  let d = new Date();
  let timestampUTC = Math.trunc(d.getTime() / 1000); //for RCT need timestamp in s
  let timezoneOffset = d.getTimezoneOffset() * 60; //offset given in minutes, need s
  let timestamp = timestampUTC - timezoneOffset;

  $.ajax({
    url: "/getData/?magicNumber=" + magicNumber + "&timestamp=" + timestamp,
    type: "GET",
    success: getDataSuccess,
    error: getDataError,
    async: true
  });

}



function buildPage() {
  app = new Vue({
    el: '#app',
    data: {
      radioButtonUnit0: '1', //value of the radio button to select the unit
      radioButtonUnit1: '1',
      radioButtonUnit2: '1',
      radioButtonUnit3: '1',
      alertShow: 0,
      alertText: "",
      alertVariant: "info",
      headline: "",
      tabActive: 0,
      chartTitle0: "Select your chart",
      chartTitle1: "Select your chart",
      chartTitle2: "Select your chart",
      chartTitle3: "Select your chart"
    },
    methods: {
      showAlertBar: function(text, time, variant) {
        this.alertShow = time;
        this.alertText = text;
        this.alertVariant = variant;
      },
      onClickBtnLoadData: function(magicNumber) { //text in map aufnehmen und in eigenes file auslagern https://flaviocopes.com/es-modules/
        //alert('ausgabe ' +  this.selCalView  );
        app.showAlertBar(mapMessageText.get(magicNumber), 30, "info");
        this.headline = mapHeadlineText.get(magicNumber); // change value for {{headline}} currently not used
        loadData(magicNumber);
      },
      changeUnit: function(newUnitConvertFactor)
      {
        radioButtonUnit[app.tabActive] = newUnitConvertFactor;
        refreshChart(newUnitConvertFactor); //data element radioButtonUnit will only change once that method has finished
      },
      changeTab: function(newTab)
      {

        //cannot clear chart in this method, needs clarification why.
        //loadData(dailyMagicNumbers[newTab]);
        //alert("new tab " + newTab);

        //empty current chart, we don't know what chart the user wants to see next
        // chart.detach();
        // data.labels = ["bla"];
        // data.series[0] = [1];
        // chart.update();
        //alert("tab ende");

        //clearChart();
      },
      onClickInfo: function()
      {
        alert("This software is provided as is. It is not any official software provided by RCT Power. The author of this software is not affiliated to RCT Power in any way.");
      }
    }
  })

  data0 = {
    // A labels array that can contain any sort of values
    labels: [],
    // Our series array that contains series objects or in this case series data arrays
    series: [
      []
    ]
  };

  data1 = {
    // A labels array that can contain any sort of values
    labels: [],
    // Our series array that contains series objects or in this case series data arrays
    series: [
      []
    ]
  };

  data2 = {
    // A labels array that can contain any sort of values
    labels: [],
    // Our series array that contains series objects or in this case series data arrays
    series: [
      []
    ]
  };

  data3 = {
    // A labels array that can contain any sort of values
    labels: [],
    // Our series array that contains series objects or in this case series data arrays
    series: [
      []
    ]
  };

  //Create a new line chart object where as first parameter we pass in a selector
  //that is resolving to our chart container element. The Second parameter
  //is the actual data object.
  chart0 = new Chartist.Bar('#chart0', data0);
  chart1 = new Chartist.Bar('#chart1', data1);
  chart2 = new Chartist.Bar('#chart2', data2);
  chart3 = new Chartist.Bar('#chart3', data3);

}
