/*
 * Copyright 2019 DZB Leipzig
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dzb.page;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;


/**
 * @author <a href="mailto:lars.voigt@dzb.de">Lars Voigt</a>
 * @version $Revision: 1 $
 */
public class ConsolePage {

    @FindBy(partialLinkText = "User Federation")
    private WebElement userFederationLink;

    @FindBy(partialLinkText = "dzb-userstore")
    private WebElement dzbUserStorageLink;

    @FindBy(xpath = "//select/option[normalize-space(text())='dzb-userstore']")
    private WebElement dzbUserStorageOption;

    @FindBy(xpath = "//button[text()[contains(.,'Save')]]")
    private WebElement save;

    @FindBy(xpath = "//td[text()[contains(.,'Delete')]]")
    private WebElement deleteBtn;

    @FindBy(xpath = "//button[text()[contains(.,'Delete')]]")
    private WebElement deleteConfirmationBtn;

    @FindBy(id = "username")
    private WebElement username;

    @FindBy(linkText = "Sign Out")
    private WebElement logoutLink;

    @FindByJQuery("input[class*='form-control']:eq(3)")
    private WebElement propertyPath;

    public void navigateToUserFederationMenu() {
        Graphene.waitGui().until(ExpectedConditions.elementToBeClickable(
                By.partialLinkText("User Federation")));
        userFederationLink.click();
    }

    public void selectdzbUserUserStorage() {
        dzbUserStorageOption.click();
    }

    public void logout() {
        logoutLink.click();
    }

    public String getUser() {
        Graphene.waitGui().until(ExpectedConditions.visibilityOfElementLocated(
                By.id("username")));
        return username.getAttribute("value");
    }

    public WebElement dzbUserStorageLink() {
        return dzbUserStorageLink;
    }

    public void createDzbUserStorage() {
        navigateToUserFederationMenu();
        selectdzbUserUserStorage();
        save.click();
    }

    public void delete() {
        navigateToUserFederationMenu();
        deleteBtn.click();
        deleteConfirmationBtn.click();
    }

}
