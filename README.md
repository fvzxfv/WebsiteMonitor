# WebsiteMonitor
Website availability &amp; performance monitor

## JUnit Test added
Use "Run As JUnit Test" to test Alerting logic

## Improvement so far
- Used user input website and interval instead of inside-code and pre-defined data
- Defined special generic type to store and print data and corresponding timeline info
- Most methods and classes are generalizable and their logic are universal

## Potential improvement
MainRunner: 
- Add "If no input, try again" logic in 'init()' method
- For now, 'isValidData()' only verifry validness of interval, will consider url as well (maybe with UrlValidtor), especially some of which contains parameters
- Will distinguish whether the check interval is the interval "after response and before next request" or "between adjacent request"

WebRequestData: 
- Add separate lists to store data for shorter interval instead of tracing data from whole lists, reduce code complexity (although won't reduce neither time nor space)
- Should considering split this class into different class to reduce complexity

Printer:
- Use more pleaseant format of output, e.g. different color etc.

Test: 
- Consider more boundary case regarding multiple thread
