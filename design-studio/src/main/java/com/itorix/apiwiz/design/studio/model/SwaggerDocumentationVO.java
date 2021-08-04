package com.itorix.apiwiz.design.studio.model;

public class SwaggerDocumentationVO {
	private String title;

	private Items[] items;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Items[] getItems() {
		return items;
	}

	public void setItems(Items[] items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "ClassPojo [title = " + title + ", items = " + items + "]";
	}

	public class Items {
		private String id;

		private Versions[] versions;

		private String title;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Versions[] getVersions() {
			return versions;
		}

		public void setVersions(Versions[] versions) {
			this.versions = versions;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return "ClassPojo [id = " + id + ", versions = " + versions + ", title = " + title + "]";
		}
	}

	public class Versions {
		private String name;

		private String url;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "ClassPojo [name = " + name + ", url = " + url + "]";
		}
	}
}
