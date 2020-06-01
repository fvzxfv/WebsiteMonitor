# WebsiteMonitor
Website availability &amp; performance monitor

## JUnit Test added
Use "Run As JUnit Test" to test Alerting logic

## Improvement so far
- Use user input website and interval instead of inside-code and pre-defined data
- Defined special generic type to store and print data and corresponding timeline info
- Most of method and class are generalizable and its logic and be used universally

## Potential improvement
MainRunner: 
- Add "If no input, try again" logic in 'init()' method
- For now, 'isValidData()' only verifry validness of interval, will consider url as well (maybe with UrlValidtor), especially some of which contains parameters
- Will distinguish whether the check interval is the interval "after response and before next request" or "between adjacent request"

WebRequestData: 
- Add separate lists to store data for shorter interval instead of tracing data from whole lists, reduce code complexity (although won't reduce neither of time or space)
- Should considering split this class into different class to reduce complexity

Printer:
- Use more pleaseant format of output, e.g. different color etc.

Test: 
- Consider more boundary case regarding multiple thread
