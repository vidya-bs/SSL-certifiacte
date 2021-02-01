package com.itorix.apiwiz.data.management.model;

import java.util.List;

public class ApigeeOrganizationalVO {
	private List<Environment> environment;

	private String name;

	public List<Environment> getEnvironment() {
		return environment;
	}

	public void setEnvironment(List<Environment> environment) {
		this.environment = environment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ApigeeOverView [environment=" + environment + ", name=" + name + "]";
	}

	public class Environment {
		private String name;

		private List<Proxies> proxies;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<Proxies> getProxies() {
			return proxies;
		}

		public void setProxies(List<Proxies> proxies) {
			this.proxies = proxies;
		}

		@Override
		public String toString() {
			return "ClassPojo [name = " + name + ", proxies = " + proxies + "]";
		}
	}

	public class Proxies {
		private List<String> targetservers;

		private String revision;

		private List<String> cache;

		private String name;

		private List<Products> products;

		private List<String> kvm;

		public List<String> getTargetservers() {
			return targetservers;
		}

		public void setTargetservers(List<String> targetservers) {
			this.targetservers = targetservers;
		}

		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

		public List<String> getCache() {
			return cache;
		}

		public void setCache(List<String> cache) {
			this.cache = cache;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<Products> getProducts() {
			return products;
		}

		public void setProducts(List<Products> products) {
			this.products = products;
		}

		public List<String> getKvm() {
			return kvm;
		}

		public void setKvm(List<String> kvm) {
			this.kvm = kvm;
		}

		@Override
		public String toString() {
			return "ClassPojo [targetservers = " + targetservers + ", revision = " + revision + ", cache = " + cache
					+ ", name = " + name + ", products = " + products + ", kvm = " + kvm + "]";
		}

		
	}
	public class Products {
		private List<Apps> apps;

		private String name;

		public List<Apps> getApps() {
			return apps;
		}

		public void setApps(List<Apps> apps) {
			this.apps = apps;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "ClassPojo [apps = " + apps + ", name = " + name + "]";
		}
	}
	public class Apps {
		private String name;

		private List<String> developers;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getDevelopers() {
			return developers;
		}

		public void setDevelopers(List<String> developers) {
			this.developers = developers;
		}

		@Override
		public String toString() {
			return "ClassPojo [name = " + name + ", developers = " + developers + "]";
		}
	}
}