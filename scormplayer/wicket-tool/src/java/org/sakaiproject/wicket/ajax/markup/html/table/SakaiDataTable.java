/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.wicket.ajax.markup.html.table;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;


public class SakaiDataTable extends AjaxFallbackDefaultDataTable {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param id
     *            component id
     * @param columns
     *            list of columns
     * @param dataProvider
     *            data provider
     * @param pageable
     *            table should have paging controls
     */
    public SakaiDataTable(String id, final List<IColumn> columns, ISortableDataProvider dataProvider, boolean pageable) {
        super(id, columns, dataProvider, 20);
        delayAddToolbars(dataProvider, pageable);
    }

    private void delayAddToolbars (ISortableDataProvider dataProvider, boolean pageable) {
        if (pageable) {
            addTopToolbar(new SakaiNavigationToolBar(this));
        }

        addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this, new ResourceModel("no_data")));
    }

    @Override
	/**
	 * Overridden method to addToolBars
	 * @param dataProvider {@link org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider}
	 */
	protected void addToolBars(final ISortableDataProvider dataProvider) {
        //Do nothing to prevent toolbar from being created in the super
    }
}
