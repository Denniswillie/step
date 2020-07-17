// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function inputChangeHandling(){
    loadRecommendations(document.querySelector(".maxNumberOfRecommendationsDisplayed").value);
}

function loadRecommendations(queryString) {

    var queryParams = "";
    if(queryString){
        queryParams = queryParams + "?max=" + queryString;
    }

  fetch('/load-recommendations' + queryParams).then(response => response.json()).then((fetchedData) => {
    
    const scrollElement = document.querySelector(".specialScroll");
    
    //if user is logged in
    if(fetchedData.isLoggedIn){
    
        displayRecommendationPage(fetchedData.recommendationsList, 
                                scrollElement, 
                                fetchedData.maxNumberofRecommendationsDisplayed, 
                                fetchedData.urlForLoginOrLogout);

    }

    //if user is not logged in, comment form will be hidden
    else{
        displayLoginPage(scrollElement, fetchedData.urlForLoginOrLogout);
    }
  
  });

}

function displayRecommendationPage(recommendationsList, scrollElement, maxNumberofRecommendationsDisplayed, logoutURL){

    document.querySelectorAll('.tickets').forEach(recommendationDiv => recommendationDiv.remove());
    
    recommendationsList.forEach((recommendation) => {
        scrollElement.appendChild(createRecommendationElement(recommendation));
    })
    document.querySelector(".maxNumberOfRecommendationsDisplayed").value = maxNumberofRecommendationsDisplayed;
    document.getElementById("logoutButton").onclick = function(){
        location.href = logoutURL;
    }

}

function displayLoginPage(scrollElement, loginURL){

    scrollElement.querySelectorAll('*').forEach(childNode => childNode.remove());
    const loginText = document.createElement("h1");
    loginText.setAttribute('class', 'loginH1');
    loginText.innerHTML = "<a href = '" + loginURL + "'>Login here to view and input recommendations</a>"
    scrollElement.appendChild(loginText);

}

function createRecommendationElement(recommendation) {
  const ticketsDiv = document.createElement("div");
  const ticketsRightDiv = document.createElement("div");
  const ticketsLeftDiv = document.createElement("div");
  const h3Element = document.createElement("h3");
  const h4Element = document.createElement("h4");
  const emailElement = document.createElement("h4");
  ticketsDiv.setAttribute('class', 'tickets');
  ticketsRightDiv.setAttribute('class', 'ticketsRight');
  ticketsLeftDiv.setAttribute('class', 'ticketsLeft');
  ticketsRightDiv.innerText = recommendation.comment;
  h3Element.innerText = recommendation.name;
  h3Element.style.marginTop = '1em';
  h3Element.style.marginTop = '2em';
  h4Element.innerText = recommendation.relationship;
  emailElement.innerText = recommendation.email;
  ticketsLeftDiv.appendChild(h3Element);
  ticketsLeftDiv.appendChild(h4Element);
  ticketsLeftDiv.appendChild(emailElement);

  ticketsDiv.appendChild(ticketsLeftDiv);

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.classList.add("btn", "btn-primary", "mb-2");
  deleteButtonElement.style.fontFamily = 'AvenirLight';
  deleteButtonElement.innerText = 'x';
  deleteButtonElement.style.zIndex = '2000000';
  deleteButtonElement.style.background = '#030bfc';
  deleteButtonElement.addEventListener('click', () => {
    deleteRecommendation(recommendation);

    // Remove the task from the DOM.
    ticketsDiv.remove();
  });
  ticketsDiv.appendChild(deleteButtonElement);
  ticketsDiv.appendChild(ticketsRightDiv);
  return ticketsDiv;
}

/** Tells the server to delete the task. */
function deleteRecommendation(recommendation) {
  const params = new URLSearchParams();
  params.append('id', recommendation.id);
  fetch('/delete-recommendation', {method: 'POST', body: params});
}