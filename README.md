Note: I have created this solution using the Java Spring Boot framework.

Recording link:

Ideas for improving the solution:

1] Partial Report Refresh Instead of Full Regeneration:
-  Instead of regenerating the entire report every time from scratch, we can design the system to only recalculate metrics for time windows that have new polling data.
-  This would improve performance, especially as data grows.

2] Business Hour Caching Layer:
-  Since business hours don't change often, we can cache them in memory or a fast key-value store like Redis, reducing joins and repeated parsing during report generation.

3] Parallelization of Report Jobs:
- In the current design, if many stores exist, the report might take time. So the report generation can be parallelized using multithreading. So that each store's calculation runs independently.

4] Data Freshness Indicators:
- Add a field in the report that shows how recent the data is for each store. This helps identify if any stores have stopped sending updates.
