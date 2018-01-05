# PhotoShare (Project developed for Air Force Research Laboratory)
A multi-threaded Android application for geo-tagged message(image) creation via Google cloud Vision API and automated sharing via multi-connect Bluetooth for Delay Tolerant Network(DTN). The back-end consists of sharing interests and a bunch of parameters between two devices when they connect. Message forwarding decisions are taken by the incentive module. 

Navigation, Point selection on map, Camera, Gallery, Speech Queries, GridView(for showing image and metadata),etc. are all included.

Working:
Discovery is initiated periodically by every device with the application installed. It searches for all the devices in the bluetooth range, connects with those which have the application installed. After connection is established, the preferences are shared:
i) Role of the user ii) Message Transfer mode: push or pull iii) Keyword based interests

ChitChat's(a state of the art data-centric routing algorithm) transient social relationship decay and growth module are executed.

After sharing of above data is done with all the connected devices in the neighborhood, message transfer module is initiated. In this module, a device looks for all the messages it can share with all the connected devices, selects a best carrier of the message based on ChitChat and incentive mechanism, which is a credit based and reputation based mechanism for data-centric message delivery in Delay tolerant Networks. Various factors are considered in the background, for example, message size, message quality, the quality of keyword based annotations, interest level for the connected devices, energy consumption in message transfer, etc.

Only some of the screenshots are shown here.  In addition to these, there are also screens to show the nodes in the path of the message, show the number of incentive tokens left, last 10 incentive token transactions, navigation functionality for navigating to any selected message(image), sharing the data via WiFi or over internet to any IP Address, if that device is running the FileServer code written in java available as a separate repository named PhotoShareFileServerWindows, adding rating value to a message.

Following image shows the home screen for the application:
<br>
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Screenshot_2017-10-13-14-23-32.png" width="250">
<br>


This screenshot shows the screen of Neighbors, neighbors' list shows all the devices in bluetooth range whereas connected devices' list shows all the devices connected via bluetooth to this device :<br>
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/MessageDetails.png" width="250"> 
<br>
This image shows the screen for selecting mode of operation a device can be in:
<br>
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/PushPull.png" width="250">
<br>
This screenshot shows the screen where a list of messages are shown:
<br>
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/ListOfMessages.png" width="250">
<br>
This screenshot shows the screen of Message Details(More metadata is available on scrolling):
<br>
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/MessageDetails.png" width="250">

There are huge number of things that this application can do, for example, speech query to "find out all the restaurants within one mile of myself". If there are messages received from some other device for this, those messages will be shown.
Some of the other screenshots are shown below:<br>
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Image1.png" width="250">
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Image2.png" width="250">
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Image3.png" width="250">
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Image4.png" width="250">
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Image5.png" width="250">
<img src="https://github.com/jethawahimanshu007/PhotoShare/blob/master/Image6.png" width="250">
