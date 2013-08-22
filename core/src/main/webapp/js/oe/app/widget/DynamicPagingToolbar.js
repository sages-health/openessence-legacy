/*
 * Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
 *                             All rights reserved.
 *
 * This material may be used, modified, or reproduced by or for the U.S.
 * Government pursuant to the rights granted under the clauses at
 * DFARS 252.227-7013/7014 or FAR 52.227-14.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
 * WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
 * LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
 * INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
 * RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
 * LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,
 * CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR
 * INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
 * FOR LOST PROFITS.
 */

/**
 * Extends the PagingToolbar to support paging when total count may or may not be returned.
 *
 * Removes move to end/last page as well as total page/record count displays when total count is unknown.
 */
(function () {
    Ext.ux.DynamicPagingToolbar = Ext.extend(Ext.PagingToolbar, {

        // Toggles display to dynamically handle if a total count is given
        getPageData: function () {
            var total = this.store.getTotalCount();
            var count = this.store.getCount();
            var activePage = Math.ceil((this.cursor + this.pageSize) / this.pageSize);
            var pages;
            if (total === 0 && count > total) {
                this.displayMsg = 'Displaying {0} - {1}';
                this.afterPageText = '';
                total = count;
                // If current set is less than page size, disable next
                pages = (total < this.pageSize ? activePage : -1);

                this.inputItem.disable();
                this.last.hide();
            } else {
                this.displayMsg = 'Displaying {0} - {1} of {2}';
                this.afterPageText = 'of {0}';
                pages = (total < this.pageSize ? 1 : Math.ceil(total / this.pageSize));

                this.inputItem.enable();
                this.last.show();
            }

            return {
                total: total,
                activePage: activePage,
                pages: pages
            };
        }
    });
})();
Ext.reg('dynamicPagingToolbar', Ext.ux.DynamicPagingToolbar);
