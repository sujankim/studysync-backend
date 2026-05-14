package com.sujan.studysync.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T>  content,       // the items on this page
        int      page,          // current page number (0-based)
        int      size,          // items per page
        long     totalElements, // total items across ALL pages
        int      totalPages,    // how many pages exist
        boolean  first,         // is this the first page?
        boolean  last           // is this the last page?
) {}
