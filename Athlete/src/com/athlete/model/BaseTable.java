package com.athlete.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;

public class BaseTable implements Serializable {
	/**
	 * @author edBaev
	 */

		private static final long serialVersionUID = 61654597015816116L;

		public class COL {
			public static final String ID = "id";
		}

		@DatabaseField(columnName = COL.ID, id = true, canBeNull = false)
		protected String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}


	}
