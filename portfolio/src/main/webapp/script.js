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

    document.querySelectorAll('.tickets').forEach(function(recommendationDiv){
        recommendationDiv.remove()
    })
    
    fetchedData.recommendationsList.forEach((recommendation) => {
      scrollElement.appendChild(createRecommendationElement(recommendation));
    })
    
    document.querySelector(".maxNumberOfRecommendationsDisplayed").value = fetchedData.maxNumberofRecommendationsDisplayed;
    
  
  });

}

function createRecommendationElement(recommendation) {
  const ticketsDiv = document.createElement("div");
  const ticketsRightDiv = document.createElement("div");
  const ticketsLeftDiv = document.createElement("div");
  const h3Element = document.createElement("h3");
  const h4Element = document.createElement("h4");
  ticketsDiv.setAttribute('class', 'tickets');
  ticketsRightDiv.setAttribute('class', 'ticketsRight');
  ticketsLeftDiv.setAttribute('class', 'ticketsLeft');
  ticketsRightDiv.innerText = recommendation.comment;
  h3Element.innerText = recommendation.name;
  h3Element.style.marginTop = '1em';
  h3Element.style.marginTop = '2em';
  h4Element.innerText = recommendation.relationship;
  ticketsLeftDiv.appendChild(h3Element);
  ticketsLeftDiv.appendChild(h4Element);

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