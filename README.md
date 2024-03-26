# FaultHunter

![image](https://user-images.githubusercontent.com/90657408/197356817-1784c814-904d-46f4-865a-d4ca8a8c7490.png)

FaultHunter is designed to be an automatic fault injection vulnerability detector for C source files. The C source code is parsed using ANTLR, processed using a Java backend, and then presented in a JavaFX GUI. FaultHunter detects vulnerable 'functional blocks' of code, referred to as patterns, based on the prior classification of fault injection patterns by Riscure (See the whitepaper: https://www.riscure.com/uploads/2018/11/201708_Riscure_Whitepaper_Side_Channel_Patterns.pdf). Currently, 'Fault.ConstantCoding', 'Fault.Branch', 'Fault.DefaultFail', and 'Fault.LoopCheck' have implementations.

## Reference
When reporting results that use the dataset or code in this repository, please cite the paper below:

*Logan Reichling, Ikran Warsame, Shane Reilly, Austen Brownfield, Nan Niu, Boyang Wang, “FaultHunter: Automatically Detecting Vulnerabilities in C against Fault Injection Attacks,” 2022 Symposium for Undergraduate Research in Data Science, Systems, and Security (REU Symposium 2022), Vancouver, Washington, December 6-9, 2022*

**The dataset and code are for research purpose only**

## Dataset 
Our datasets used in this study can be accessed through the link below (last modified March 2024):

https://mailuc-my.sharepoint.com/:f:/g/personal/wang2ba_ucmail_uc_edu/EgHHSqi_FmBMtKZvzbsLtnwB_a9fcrluOMVPfZI-datD2A?e=YPcJjS

Note: the above link need to be updated every 6 months due to certain settings of OneDrive. If you find the links are expired and you cannot access the data, please feel free to email us (boyang.wang@uc.edu). We will be update the links as soon as we can. Thanks!


# Content 
This repository contains a IntelliJ Maven project of FaultHunter using Java 11. The ANTLR library will have to downloaded and pointed to externally. See the requirements section for more details. 
Directories:
* ```gen/com/afivd/afivd/```               - Contains ANTLR-generated source files that are used by our program to see the parsed entries. 
* ```out/artifacts/afivd_jar```            - Contains most recent created self-contained jar. 
* ```src/main/java/com/afivd/afivd```      - Contains all source code and the ANTLR grammar file (C.g4, available https://github.com/antlr/grammars-v4/blob/master/c/C.g4). 
* ```src/main/resources/com/afivd/afivd``` - Contains GUI scene file and application icon
# Requirements
The following dependencies will need to be downloaded for further development: 
* Java 11 JDK (Amazon Corretto 11.0.15 JDK used in our case)
* ANTLR-4.10.1 from https://www.antlr.org/download.html, select Java (other dependencies should be downloaded automatically by Maven)

No internet connection or powerful computer needed to run the program. 
# Usage
Files are loaded into the application using a file picker window. Basic usage instructions:
1. Open the FaultHunter application.
2. Press the "Load C File" button.
3. Choose the C file you wish to test using the new file picker window. Then, press "Open".
4. Press the "Run" button to run the tool. Check the "Show Tree" box if you also wish to see the resultant parse tree (may take over ten seconds if checked).

The "Comments:" box is now populated with suggestions to reduce fault injection vulnerabilities. Certain patterns also provide additions/modifications to the code that remediate vulnerabilities. You can view these by clicking the "Show Replacements" radio button. ***Disclaimer***: This tool is designed to assist with vulnerability remediation efforts and not completely replace human judgement: false positives and false negatives may exist. 
