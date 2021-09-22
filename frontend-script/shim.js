function isKotlinScriptNode(node) {
  if (node === undefined) {
    return false;
  }
  return node.tagName === 'SCRIPT' && node.type === 'language/kotlin';
}

function getScriptContent(node) {
  if (node === undefined) {
    return undefined;
  }
  return node.textContent;
}

function getKotlinScriptMutation(mutation) {
  if (mutation === undefined) {
    return undefined;
  }

  if (mutation.type === 'childList') {
    for (const node of mutation.addedNodes) {
      if (isKotlinScriptNode(node)) {
        return getScriptContent(node);
      }
    }
  }
  if ((mutation.type === 'characterData' || mutation.type === 'characterData')
    && isKotlinScriptNode(mutation.target.parentNode)) {
    return getScriptContent(mutation.target.parentNode);
  }
  return undefined;
}

function handleDocumentMutation(mutations) {
  for (const mutation of mutations) {
    const script = getKotlinScriptMutation(mutation);
    if (script !== undefined){
      console.log(script);
    }
  }
}

const observer = new MutationObserver(handleDocumentMutation);
const options = {
  childList: true,
  attributes: true,
  characterData: true,
  subtree: true,
  attributeFilter: ['type'],
  attributeOldValue: false,
  characterDataOldValue: false
};

const htmlNode = document.querySelector('html');
observer.observe(htmlNode, options);
