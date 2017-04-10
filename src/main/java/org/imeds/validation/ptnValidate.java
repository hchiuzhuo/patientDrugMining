package org.imeds.validation;

public class ptnValidate {

		String sid;		
		Integer id;
		Double coef;
		Double pvalue;
		Double adjpvalue;
		public ptnValidate(String sid, Integer id,Double coef, Double pvalue) {
			super();
			this.sid = sid;
			this.id = id;
			this.coef=coef;
			this.pvalue = pvalue;
		}
		public String getSid() {
			return sid;
		}
		public void setSid(String sid) {
			this.sid = sid;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public Double getCoef() {
			return coef;
		}
		public void setCoef(Double coef) {
			this.coef = coef;
		}
		public Double getPvalue() {
			return pvalue;
		}
		public void setPvalue(Double pvalue) {
			this.pvalue = pvalue;
		}
		public Double getAdjPvalue() {
			return adjpvalue;
		}
		public void setAdjPvalue(Double adjpvalue) {
			this.adjpvalue = adjpvalue;
		}
		@Override
		public String toString() {
			return "pvalue [sid=" + sid + ", id=" + id + ", coef=" + coef+  ", pvalue=" + pvalue
					+ ", adjpvalue=" + adjpvalue +"]";
		}
	
}
