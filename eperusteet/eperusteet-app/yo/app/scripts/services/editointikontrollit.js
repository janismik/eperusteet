/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
* 
* This program is free software: Licensed under the EUPL, Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
* 
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

'use strict';

angular.module('eperusteApp')
  .factory('Editointikontrollit', function() {
    var editingCallback = null;
    return {
      startEditing: function() {
        if(editingCallback) {
          editingCallback.edit();
        }
      },
      saveEditing: function() {
        if(editingCallback) {
          editingCallback.save();
        }
      },
      cancelEditing: function() {
        if(editingCallback) {
          editingCallback.cancel();
        }
      },
      registerEditingCallback: function(callback) {
        if(!callback || !callback.edit || !callback.save || !callback.cancel) {
          console.error('callback-function invalid');
          throw 'editCallback-function invalid';
        }
        editingCallback = callback;
      },
      unregisterEditingCallback: function() {
        editingCallback = null;
      },
      editingEnabled: function() {
        if(editingCallback) {
          return true;
        } else {
          return false;
        }
      }
    };
});
