const { createClient, gql } = require('urql');

const client = createClient({
  url: 'http://localhost:4000/graphql',
});

const query = gql`
  query {
    elements {
      element_type {
        __typename
      }
      id
      name
    }
  }
`;

client.query(query).toPromise()
  .then(result => console.log(result.data))
  .catch(error => console.error(error));

  
// fetch('http://localhost:4000/graphql', {
//   method: 'POST',
//   headers: { 'Content-Type': 'application/json' },
//   body: JSON.stringify({ jquery })
// })
//     .then(response => console.log(response))
// //   .then(response => response.json())
// //   .then(data => {
// //     console.log(data);
// //     const elements = data.data.elements;
// //     elements.forEach(element => {
// //       console.log(element.name);
// //     });
// //   })
//   .catch(error => {
//     console.error('Error:', error);
//   });
