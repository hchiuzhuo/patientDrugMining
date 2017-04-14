# patientDrugMining
This project leverages sequential pattern mining to discover patients' frequent drug usage pattern and then apply cox regression to determine hazard pattern. 
Details can be referred to project_v1.pdf.

By taking advantage of the computation power provided by spark, we implement cox regression on spark and run experiences on 16 nodes. Result shows that cox regression cannot fully get benefit from spark.
Details can be referred to project_v2.pdf.
