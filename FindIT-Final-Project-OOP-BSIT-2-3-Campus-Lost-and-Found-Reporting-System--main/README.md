FindIT: A Campus Lost and Found Reporting System is a desktop-based application designed to centralize the reporting, monitoring, and management of lost and found items within the campus. The system aims to replace the inefficient process of relying on scattered social media posts, manual logs, and verbal reporting by providing an organized and accessible platform for students and administrators. 

Details about the application: 
* **Language:** Java 17
* **GUI Framework:** JavaFX
* **Build Tool:** Maven
* **Database:** Supabase / JDBC

Features: 
1) Report Lost & Found Item
2) Claim Item 
3) Shows Lost & Found Item Details
4) Shows Claim History
5) Admin Dashboard
6) Shows History of All Reported Items (Admin Exclusive)
7) Match Suggestions (Admin Exclusive)
8) Ingress/Egress Monitoring (Admin Exclusive)
9) Undo/Revert Claim Changes (Admin Exclusive)
10) Approve/Reject Claim (Admin Exclusive)
11) Help Tab
12) Search Bar and Status Filters for Easy Access
13) Item and Claim Tracking ID for Editing & Deleting Item/Claim Reports
        
How to Install? 
1) Access the application's GitHub link: https://github.com/lloydgabriel/FindIT-Final-Project-OOP-BSIT-2-3-Campus-Lost-and-Found-Reporting-System-
2) Once accessed, click "Code" and then "Download ZIP"
3) After installation, you can run our application in any IDE that supports Java (IntelliJ, VSCode with Java extension, etc.)

How to Run?
1) Once the folder is opened in any IDE of your choice that supports Java, you can access the application by
clicking the src folder: src -> main -> java -> com/example/findit -> Launcher.java
2) Run Launcher.java and you should be able to run FindIT!

PROJECT FLOW:
```text
FindIT-Final-Project-OOP-BSIT/
├── pom.xml                               # Maven dependencies and Java 17 compiler config
├── README.md                             # Project documentation
├── src/
│   ├── create_user_tables.sql            # SQL script to initialize the database schema
│   │
│   └── main/
│       ├── java/com/example/findit/
│       │   ├── Launcher.java             # Safely launches the JavaFX application
│       │   ├── ProjectApplication.java   # Main JavaFX Stage and Scene configuration
│       │   ├── module-info.java          # Java Module System configuration
│       │   │
│       │   ├── controllers/              # UI LOGIC & EVENT HANDLERS
│       │   │   ├── MainPortalController.java
│       │   │   ├── RegistrationController.java
│       │   │   ├── admin/                # Admin-specific logic (Dashboard, Claims, Matches)
│       │   │   └── user/                 # User-specific logic (Forms, Gallery, Navigation)
│       │   │
│       │   ├── dao/                      # DATABASE OPERATIONS (Data Access Objects)
│       │   │   ├── ActivityLogDAO.java
│       │   │   └── UserDAO.java
│       │   │
│       │   ├── model/                    # DATA BLUEPRINTS
│       │   │   └── User.java
│       │   │
│       │   └── util/                     # HELPER CLASSES
│       │       └── DBConnection.java     # JDBC connection credentials
│       │
│       └── resources/com/example/findit/
│           ├── assets/                   # ALL IMAGES & ICONS (png, jpg)
│           │   └── yellow_icons/         # Active-state dynamic UI icons
│           │
│           └── views/                    # FXML VISUAL LAYOUTS
│               ├── admin/                # Admin portal screens (AdminDashboard, Claims, etc.)
│               └── user/                 # User portal screens (FoundForm, Dashboard, etc.)
