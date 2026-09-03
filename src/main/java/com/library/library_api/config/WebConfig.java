package com.library.library_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

// Fixes the "Serializing PageImpl instances as-is is not supported" warning.
// VIA_DTO makes Spring serialize every Page response through a STABLE, documented DTO
// (PagedModel) instead of the raw PageImpl, whose JSON shape isn't guaranteed to stay the
// same across Spring versions. With this on, a paged response looks like:
//   { "content": [ ... ], "page": { "size": 20, "number": 0,
//                                    "totalElements": 8, "totalPages": 1 } }
// i.e. the paging metadata is grouped under "page" - a contract clients can rely on.
@Configuration
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {
}
