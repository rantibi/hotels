# Hotels
This repository is a solution for the following exercise:

You are provided with hotels database in CSV (Comma Separated Values) format.
We need you to implement HTTP service, according to the API requirements described below. 
1.  RateLimit: API calls need to be rate limited (request per 10 seconds) based on API Key provided in each http call.
     *    On exceeding the limit, api key must be suspended for next 5 minutes.
     *   Api key can have different rate limit set, in this case from configuration, and if not present there must be a global rate limit applied.
2.  Search hotels by CityId
3.  Provide optional sorting of the result by Price (both ASC and DESC order).

https://github.com/itplatform/hotelapi

# Constraints
1. The memory can hold all the CSV.
2. Seed a few example API keys and hold them in Memory on startup.
3. The CSV can not change while the application is running.
4. The API response should be json.

# Configuration
You can find an exmple of the configuration file under `test` folder.

# Execution
You should run `Application` class, and provider single command line argument with the path to the configuration file.
After running you can get to:
`http://127.0.0.1:8080/hotels/{API_KEY}/search?city={CITY}&order={ASC/DESC}`
