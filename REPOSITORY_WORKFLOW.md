# Repository workflow

One repository per product, one version branch containing supported loader
modules. This MVP uses `26.3` and Fabric only. Maintain source, changelog, tests,
and exact artifact hashes together. Remote repository/publication has not been
requested. Do not add fabricated source or issue URLs to metadata.

Verify each future loader/version separately, keep SeamlessLib separate, and
record built/tested/committed/pushed/published states accurately. QA artifacts
must never enter the runtime jar.
