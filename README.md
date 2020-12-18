# FruitVeg Freshness Detection using Deep Learning in Python along with Android App

## Scope
The project scope is limited to freshness detection of 3 fruits (Apple, Orange and Banana) and 3 vegetables (Cucumber, Tomato, Lemon). Moreover, scope of the project includes collection of images dataset of fruits and vegetables. These images would be preprocessed and placed in directories based on their freshness level. We’ll develop a deep learning model for classification of fruits and vegetables either as fresh, mild rotten or rotten by training on the gathered dataset i.e. it is a multi-class problem. An android application would be developed that’ll allow the user to take images of fruits and vegetables. These images would be forwarded to the remote server on which the classification deep learning CNN model based on VGG-16 architecture would have been deployed and the model would predict the class of the forwarded image as fresh, medium fresh or rotten. This result would be presented to the user. However, the user has-to manually select the type of fruit or vegetable before sending the image to the server.

## Dataset
For training and testing, the dataset was gathered using fruits 360 dataset which is publically available on Kaggle and capturing our own dataset for the fruits and vegetables which were not available in that dataset. Our dataset contains total of different fruits and vegetables pictures of 6 categories. Each category here represents one type of fruit or vegetable. The chosen categories are apple, banana, orange, tomato, lemon and cucumber. These categories are chosen for the reason because some fruits and vegetables have similar appearances and are frequently brought into the market. Limitations to dataset have been applied in order to not make the project so extensive. The limitations follow as that all types of one fruit reside under that category. This means that all the types of apples reside under the apple category and similarly goes for all other classes of fruits and vegetables. Firstly, individual classifiers were trained on individual fruit and vegetable separately and then a combined single classifier was trained and tested with the all collective fruits and vegetables i.e. All fresh fruits and vegetables were placed in "fresh" class, all rotten fruits and vegetables were placed in "rotten" class and similarly all medium fresh fruits and vegetables were placed in "medium" class. We have only deployed the Single Collective Classifier. For training the model 70% (27529) images were used and for testing 30% (13545) images were used. The dataset is split into three categories for each class i.e. fresh, medium fresh and rotten.

##### Following table shows the training and testing images per fruit and vegetable:
- Total Training Images = 27529
- Total Test Images = 13545

- Apple     - Train:6854 | Test:2940
- Banana    - Train:3812 | Test:1632
- Orange    - Train:3468 | Test:1487
- Tomato    - Train:6647 | Test:3187
- Cucumber  - Train:1400 | Test:599
- Lemon     - Train:5752 | Test:2464

###### Complete Dataset can be downloaded from [(https://www.kaggle.com/usamarasheed/complete-fruit-veg-dataset-v1)]
###### Trained CNN Classifier can be downloaded from [(https://www.kaggle.com/usamarasheed/multi-class-freshness-classifier)]

## Implementation Details
The network is trained for 50 epochs with a batch size of 32. The technique of early stopping is applied where if the loss is not reduced consecutively for 4 epochs then training is stopped to avoid the overfitting of the model and Model Checkpoint technique is used to save the best model i.e. with minimum validation loss. The average accuracy of CNN found by applying 3-fold cross validation for six independent classifier i.e. apple, banana, orange, tomato, lemon and cucumber is 86.72%, 85.85%, 87.39%, 88.58%, 86.38% and 85.97% respectively. Similarly the average accuracy of CNN found by applying 3-fold cross validation for single classifier is 83.75%. The training accuracies of six independent classifier for apple, banana, orange, tomato, lemon, and cucumber is 84.68%, 82.34%, 85.63%, 88.97%, 83.53% and 84.1% respectively and testing accuracies are 87.35%, 85.81%, 86.40%, 89.9%, 84.97% and 83.32% respectively. Similarly the training accuracy of Single Collective Classifier is 84.88% and testing accuracy is 84.2%.
Moreover, an Android Application has been developed using JAVA to make use of our Freshness Classifier.


## Files Details
- FYPApp is the android application on Client Side
- binary_class_training_testing.ipynb file contains code for training the CNN model on Training Data and then testing it on Test Data in binary class problem
- multi_class_training_testing.ipynb file contains code for training the CNN model on Training Data and then testing it on Test Data in multi class problem
- binary_class_3FoldCrossValidation.ipynb file contains code for performing 3Fold Cross Validation on CNN Classifier in a Binary Class Problem i.e. Fresh and Rotten classes only
- multi_class_3FoldCrossValidation.ipynb file contains code for performing 3Fold Cross Validation on a CNN Classifier in a Multi Class Problem i.e. Fresh, Medium and Rotten classes
- server-side-code.ipynb contains server side code that accepts images through sockets and then predicts their class py loading the trained CNN Classifier

## App Manual
1. Install the Application on Android Device
2. Open the Application
3. Select Capture Image to capture image using camera/ Select Load Picture to load from Gallery
4. Select Category of Fruit/Vegetable from dropdown list.
5. After selecting image, its freshness label would be displayed after prediction from CNN model from the server.
