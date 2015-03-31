/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.disc.teslademo;

public class Constants {

    public static final String AWS_ACCOUNT_ID = "644469153533";
    public static final String IDENTITY_POOL_ID = "us-east-1:01c589d1-d65e-4c2b-b0f5-15f72bf52053";
    public static final String UNAUTH_ROLE_ARN = "arn:aws:iam::644469153533:role/Cognito_TeslaAppPoolUnauth_DefaultRole";
    public static final String AUTH_ROLE_ARN = "arn:aws:iam::644469153533:role/Cognito_TeslaAppPoolAuth_DefaultRole";
    // Note that spaces are not allowed in the table name
    public static final String TEST_TABLE_NAME = "NewTable";
    public static final String CourseTableName = "CourseData";
    public static final String UserTableName = "UserData";
    public static final String PlayedGameTableName = "GameData";


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


}
