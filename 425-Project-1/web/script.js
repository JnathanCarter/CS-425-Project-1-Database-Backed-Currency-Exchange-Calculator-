var Lab4 = (function () {

    var rates = null;

    var createCurrencyMenu = function (currencies) {

        // INSERT YOUR CODE HERE

        $("#currencylist").append("<select title=\"currencymenu\" id=\"currencymenu\">" + "</select name>");
        /*for (const currency in currencies) {

            $("#currencymenu").append("<option value=\"" + currency.description + "\">" + currency.id);
        }
        */
        currencies.forEach(element => {
            $("#currencymenu").append("<option value=\"" + element['id'] + "\">" + element['id'] + " (" + element['description'] + ")");
        });
    };

    var convert = function (rate_date) {
        // INSERT YOUR CODE HERE

        //log all rates to console
        for (const key in rates) {
            console.log(key + "----" + rates[key]);
        }

        var input_usd = $("#usd_value").val();

        var input_eur = convertToEruo(input_usd);

        var targetCurrency = $("#currencymenu").val().trim();

        var targetRate = rates[targetCurrency];



        var result;

        var formatterForTarget = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: targetCurrency,
        });

        result = formatterForTarget.format(targetRate * input_eur);


        var formatterUSD = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        });

        $("#output").html("The equivalent of " + formatterUSD.format(input_usd) + " in " + targetCurrency + " for the date " + rate_date + " is: " + result);
    };

    var getRatesAndConvert = function (key, rate_date) {

        console.log("Getting rates for " + rate_date + " ...");
        // INSERT YOUR CODE HERE

        var url = 'http://localhost:8180/425-Project-1/RateServlet/';

        url += rate_date;
        url += "?key=";
        url += key;

        console.log("custom url----  " + url);

        $.ajax({
            url: url,
            method: 'GET',
            dataType: 'json',
            success: function (response) {
                rates = response.rates;
                convert(rate_date);

            }
        })


    };

    function convertToEruo(usd) {
        usd_rate = rates['USD'];
        eur = usd / usd_rate;
        return eur;
    };


    return {

        getCurrenciesList: function () {

            // INSERT YOUR CODE HERE
            $.ajax({
                url: 'http://localhost:8180/425-Project-1/CurrencyServlet',
                method: 'GET',
                dataType: 'json',
                success: function (response) {

                    createCurrencyMenu(response);

                }
            })




        },

        onClick: function () {

            var rate_date = $("#rate_date").val();
            var key = $('#apikey').val();

            if (rate_date === "") {
                alert("Please enter or select a date in the \"Date\" field!");
            }
            else {

                // if rates have not been retrieved yet, or if the date is different, fetch new rates

                if ((rates === null) || (rate_date !== rates["date"])) {
                    getRatesAndConvert(key, rate_date);
                }

                // if rates for the selected date are already available, perform the conversion

                else {
                    convert(rate_date);
                }

            }

        }

    };

})();
